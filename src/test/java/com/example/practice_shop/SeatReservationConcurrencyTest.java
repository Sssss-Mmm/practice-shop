package com.example.practice_shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.constant.Role;
import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.constant.ShowtimeStatus;
import com.example.practice_shop.dtos.ticketing.SeatSelectionRequest;
import com.example.practice_shop.entity.Event;
import com.example.practice_shop.entity.Reservation;
import com.example.practice_shop.entity.Seat;
import com.example.practice_shop.entity.SeatInventory;
import com.example.practice_shop.entity.Showtime;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.entity.Venue;
import com.example.practice_shop.exception.CustomException;
import com.example.practice_shop.integration.toss.TossPaymentClient;
import com.example.practice_shop.repository.ReservationRepository;
import com.example.practice_shop.repository.SeatInventoryRepository;
import com.example.practice_shop.service.SeatRealtimeService;
import com.example.practice_shop.service.TicketingService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 같은 좌석에 N명이 동시에 예매를 시도하면 정확히 1명만 성공해야 한다.
 * 비관적 락(SELECT ... FOR UPDATE)은 DB마다 동작이 달라서 실제 Postgres로 검증한다. Docker 필요.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(TicketingService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED) // 스레드마다 별도 트랜잭션이 커밋된 데이터를 봐야 함
class SeatReservationConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @MockitoBean SeatRealtimeService seatRealtimeService;
    @MockitoBean TossPaymentClient tossPaymentClient;

    @Autowired TicketingService ticketingService;
    @Autowired SeatInventoryRepository seatInventoryRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired EntityManager em;
    @Autowired TransactionTemplate tx;

    /** 공연장·회차 1개, 좌석 seats개, 유저 users명(u0@t.com…). 반환: [showtimeId, seatId1, seatId2, ...] */
    private long[] fixture(int seats, int users) {
        return tx.execute(st -> {
            Venue venue = Venue.builder().name("v").build();
            em.persist(venue);
            Event event = Event.builder().title("e").status(EventStatus.ON_SALE).venue(venue).build();
            em.persist(event);
            Showtime showtime = Showtime.builder().event(event).venue(venue)
                    .startDateTime(LocalDateTime.now().plusDays(1)).status(ShowtimeStatus.ON_SALE).build();
            em.persist(showtime);
            long[] ids = new long[seats + 1];
            ids[0] = showtime.getId();
            for (int i = 0; i < seats; i++) {
                Seat seat = Seat.builder().venue(venue).seatNumber("A" + i)
                        .basePrice(BigDecimal.valueOf(10000)).status(SeatStatus.AVAILABLE).build();
                em.persist(seat);
                em.persist(SeatInventory.builder().showtime(showtime).seat(seat)
                        .price(BigDecimal.valueOf(10000)).status(SeatStatus.AVAILABLE).build());
                ids[i + 1] = seat.getId();
            }
            for (int i = 0; i < users; i++) {
                em.persist(User.builder().email("u" + i + "@t.com").name("u" + i).role(Role.USER).build());
            }
            return ids;
        });
    }

    @AfterEach
    void cleanUp() {
        tx.executeWithoutResult(st -> {
            for (String entity : List.of("Payment", "ReservationSeat", "Reservation", "SeatInventory", "Showtime", "Event", "Seat", "User", "Venue")) {
                em.createQuery("delete from " + entity).executeUpdate();
            }
        });
    }

    @Test
    void 같은_좌석_동시_예매는_한_건만_성공한다() throws Exception {
        int threads = 100;
        long[] ids = fixture(1, threads); // [showtimeId, seatId...]
        SeatSelectionRequest request = SeatSelectionRequest.builder()
                .showtimeId(ids[0]).seatIds(List.of(ids[1])).build();

        AtomicInteger success = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            String email = "u" + i + "@t.com";
            pool.submit(() -> {
                start.await();
                try {
                    ticketingService.createReservation(email, request);
                    success.incrementAndGet();
                } catch (Exception ignored) {
                    // SEAT_ALREADY_RESERVED 기대
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        assertThat(success.get()).isEqualTo(1);
        assertThat(reservationRepository.findAll())
                .filteredOn(r -> r.getStatus() == ReservationStatus.PENDING_PAYMENT)
                .hasSize(1);
    }

    /**
     * 같은 예약에 취소와 결제 승인이 동시에 들어와도 결과가 어긋나면 안 된다.
     * - 취소가 성공했다면 최종 상태는 CANCELLED, 좌석은 AVAILABLE
     * - 그런데 결제 승인도 성공했다면(승인 → 취소 순서) 반드시 환불이 나가야 한다
     * 락이 없으면 둘이 같은 PENDING_PAYMENT를 읽고 서로 덮어써서, "취소 성공 응답 + 환불 없이 PAID"가 나온다.
     */
    @Test
    void 같은_예약의_취소와_결제승인이_동시에_와도_돈과_좌석이_어긋나지_않는다() throws Exception {
        int n = 50;
        long[] ids = fixture(n, n);
        List<Long> reservationIds = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            reservationIds.add(ticketingService.createReservation("u" + i + "@t.com",
                    SeatSelectionRequest.builder().showtimeId(ids[0]).seatIds(List.of(ids[i + 1])).build())
                    .getReservationId());
        }
        Map<Long, String> orderIds = new HashMap<>();
        reservationRepository.findAllById(reservationIds).forEach(r -> orderIds.put(r.getId(), r.getOrderId()));

        Map<Long, boolean[]> outcomes = new HashMap<>(); // reservationId -> [cancelOk, confirmOk]
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < n; i++) {
            String email = "u" + i + "@t.com";
            Long id = reservationIds.get(i);
            String orderId = orderIds.get(id);
            CountDownLatch start = new CountDownLatch(1);
            Future<Boolean> cancel = pool.submit(() -> { start.await(); return succeeds(() -> ticketingService.cancelReservation(email, id)); });
            Future<Boolean> confirm = pool.submit(() -> { start.await(); return succeeds(() -> ticketingService.confirmPayment(orderId, "pk_" + id, 10000L)); });
            start.countDown();
            outcomes.put(id, new boolean[]{cancel.get(30, TimeUnit.SECONDS), confirm.get(30, TimeUnit.SECONDS)});
        }
        pool.shutdown();

        List<String> broken = new ArrayList<>();
        tx.executeWithoutResult(st -> outcomes.forEach((id, ok) -> {
            Reservation r = reservationRepository.findById(id).orElseThrow();
            SeatStatus seat = r.getReservationSeats().get(0).getSeatInventory().getStatus();
            boolean refunded = mockingDetails(tossPaymentClient).getInvocations().stream()
                    .anyMatch(inv -> inv.getMethod().getName().equals("cancelPayment") && ("pk_" + id).equals(inv.getArgument(0)));
            if (ok[0] && (r.getStatus() != ReservationStatus.CANCELLED || seat != SeatStatus.AVAILABLE)) {
                broken.add(id + ": 취소 성공 응답인데 최종 " + r.getStatus() + "/" + seat);
            } else if (ok[0] && ok[1] && !refunded) {
                broken.add(id + ": 결제·취소 둘 다 성공했는데 환불 없음");
            } else if (!ok[0] && ok[1] && (r.getStatus() != ReservationStatus.PAID || seat != SeatStatus.SOLD)) {
                broken.add(id + ": 결제만 성공했는데 최종 " + r.getStatus() + "/" + seat);
            }
        }));
        assertThat(broken).isEmpty();
    }

    private static boolean succeeds(Runnable action) {
        try {
            action.run();
            return true;
        } catch (CustomException e) {
            return false; // 상태 검사에 걸려 거절됨
        }
    }
}

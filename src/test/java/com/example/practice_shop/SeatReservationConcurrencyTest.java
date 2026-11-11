package com.example.practice_shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.constant.Role;
import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.constant.ShowtimeStatus;
import com.example.practice_shop.dtos.ticketing.SeatSelectionRequest;
import com.example.practice_shop.entity.Event;
import com.example.practice_shop.entity.Seat;
import com.example.practice_shop.entity.SeatInventory;
import com.example.practice_shop.entity.Showtime;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.entity.Venue;
import com.example.practice_shop.integration.toss.TossPaymentClient;
import com.example.practice_shop.repository.ReservationRepository;
import com.example.practice_shop.repository.SeatInventoryRepository;
import com.example.practice_shop.service.SeatRealtimeService;
import com.example.practice_shop.service.TicketingService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

    @Test
    void 같은_좌석_동시_예매는_한_건만_성공한다() throws Exception {
        int threads = 100;
        long[] ids = tx.execute(s -> {
            Venue venue = Venue.builder().name("v").build();
            em.persist(venue);
            Event event = Event.builder().title("e").status(EventStatus.ON_SALE).venue(venue).build();
            em.persist(event);
            Showtime showtime = Showtime.builder().event(event).venue(venue)
                    .startDateTime(LocalDateTime.now().plusDays(1)).status(ShowtimeStatus.ON_SALE).build();
            em.persist(showtime);
            Seat seat = Seat.builder().venue(venue).seatNumber("A1")
                    .basePrice(BigDecimal.valueOf(10000)).status(SeatStatus.AVAILABLE).build();
            em.persist(seat);
            em.persist(SeatInventory.builder().showtime(showtime).seat(seat)
                    .price(BigDecimal.valueOf(10000)).status(SeatStatus.AVAILABLE).build());
            for (int i = 0; i < threads; i++) {
                em.persist(User.builder().email("u" + i + "@t.com").name("u" + i).role(Role.USER).build());
            }
            return new long[]{showtime.getId(), seat.getId()};
        });
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
}

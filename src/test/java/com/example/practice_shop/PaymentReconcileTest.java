package com.example.practice_shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.entity.Reservation;
import com.example.practice_shop.entity.ReservationSeat;
import com.example.practice_shop.entity.SeatInventory;
import com.example.practice_shop.entity.Showtime;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.integration.toss.TossPaymentClient;
import com.example.practice_shop.integration.toss.TossPaymentClient.PaymentLookup;
import com.example.practice_shop.repository.PaymentRepository;
import com.example.practice_shop.repository.ReservationRepository;
import com.example.practice_shop.service.SeatRealtimeService;
import com.example.practice_shop.service.TicketingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

/**
 * 승인 응답을 못 받아 PENDING_CONFIRM으로 남은 예약을 토스 결제 조회로 정리하는지.
 */
class PaymentReconcileTest {

    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final TossPaymentClient toss = mock(TossPaymentClient.class);
    private final TicketingService service = new TicketingService(
            null, null, null, reservationRepository, mock(SeatRealtimeService.class), toss, paymentRepository,
            HoldAndQueueGateTest.directTx());

    private Reservation stuck;
    private SeatInventory seat;

    @BeforeEach
    void setUp() {
        Showtime showtime = Showtime.builder().id(1L).build();
        seat = SeatInventory.builder().id(10L).showtime(showtime)
                .price(BigDecimal.valueOf(10000)).status(SeatStatus.RESERVED).build();
        stuck = Reservation.builder()
                .id(1L)
                .user(User.builder().email("u@t.com").build())
                .showtime(showtime)
                .totalPrice(10_000)
                .status(ReservationStatus.PENDING_CONFIRM)
                .orderId("tck-1")
                .paymentKey("pk_1")
                .confirmRequestedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        stuck.getReservationSeats().add(ReservationSeat.builder().reservation(stuck).seatInventory(seat).build());

        when(reservationRepository.findByStatusAndConfirmRequestedAtBefore(any(), any())).thenReturn(List.of(stuck));
        when(reservationRepository.findByOrderId("tck-1")).thenReturn(Optional.of(stuck));
    }

    @Test
    void 토스에서_결제가_완료됐으면_예매를_확정한다() {
        when(toss.getPaymentByOrderId("tck-1")).thenReturn(new PaymentLookup("DONE", 10_000L));

        service.reconcilePendingConfirms();

        assertThat(stuck.getStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);
        verify(paymentRepository).save(any());
    }

    @Test
    void 승인되지_않은_것이_확실하면_결제_대기로_되돌려_회수에_맡긴다() {
        for (String status : List.of("ABORTED", "EXPIRED", "CANCELED")) {
            setUp();
            when(toss.getPaymentByOrderId("tck-1")).thenReturn(new PaymentLookup(status, 10_000L));

            service.reconcilePendingConfirms();

            assertThat(stuck.getStatus()).as(status).isEqualTo(ReservationStatus.PENDING_PAYMENT);
            assertThat(stuck.getPaymentKey()).as(status).isNull();
        }
    }

    @Test
    void 토스에_결제_자체가_없으면_되돌린다() {
        when(toss.getPaymentByOrderId("tck-1")).thenReturn(null); // 404

        service.reconcilePendingConfirms();

        assertThat(stuck.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
    }

    @Test
    void 아직_진행_중이거나_조회가_실패하면_건드리지_않는다() {
        when(toss.getPaymentByOrderId("tck-1")).thenReturn(new PaymentLookup("IN_PROGRESS", 10_000L));
        service.reconcilePendingConfirms();
        assertThat(stuck.getStatus()).isEqualTo(ReservationStatus.PENDING_CONFIRM);

        when(toss.getPaymentByOrderId("tck-1")).thenThrow(new ResourceAccessException("timeout"));
        service.reconcilePendingConfirms();
        assertThat(stuck.getStatus()).isEqualTo(ReservationStatus.PENDING_CONFIRM);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.RESERVED);
    }

    @Test
    void 결제_금액이_다르면_확정하지_않는다() {
        when(toss.getPaymentByOrderId("tck-1")).thenReturn(new PaymentLookup("DONE", 1_000L));

        service.reconcilePendingConfirms();

        assertThat(stuck.getStatus()).isEqualTo(ReservationStatus.PENDING_CONFIRM);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void 승인_응답을_기다리는_사이_대사가_먼저_확정해도_한_번만_확정한다() {
        stuck.setStatus(ReservationStatus.PENDING_PAYMENT);
        when(toss.getPaymentByOrderId("tck-1")).thenReturn(new PaymentLookup("DONE", 10_000L));
        // 토스 승인 응답이 늦는 동안 대사 스케줄러가 돌아 먼저 확정해 버린다
        when(toss.confirmPayment(any())).thenAnswer(inv -> {
            service.reconcilePendingConfirms();
            return null;
        });

        service.confirmPayment("tck-1", "pk_1", 10_000L);

        assertThat(stuck.getStatus()).isEqualTo(ReservationStatus.PAID);
        verify(paymentRepository).save(any()); // 결제 기록 1건
        assertThat(stuck.getUser().getTotalSpend()).isEqualTo(10_000L); // 누적 금액도 한 번만
    }
}

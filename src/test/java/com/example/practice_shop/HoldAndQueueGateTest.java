package com.example.practice_shop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.dtos.ticketing.SeatStatusMessage;
import com.example.practice_shop.entity.Reservation;
import com.example.practice_shop.entity.ReservationSeat;
import com.example.practice_shop.entity.SeatInventory;
import com.example.practice_shop.entity.Showtime;
import com.example.practice_shop.integration.toss.TossPaymentClient;
import com.example.practice_shop.repository.QueueRepository;
import com.example.practice_shop.repository.ReservationRepository;
import com.example.practice_shop.service.QueueService;
import com.example.practice_shop.service.SeatRealtimeService;
import com.example.practice_shop.service.TicketingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.ResourceAccessException;

/**
 * 스프링 컨텍스트/DB 없이 도는 최소 검증.
 * 1) 결제 대기로 방치된 예약의 좌석이 회수되는지
 * 2) 대기열 토큰 없이 예매가 통과되지 않는지
 * 3) 좌석 브로드캐스트가 커밋 이후에만 나가는지
 * 4) 외부 결제 승인이 실패했을 때 좌석 점유가 어떻게 남는지
 */
class HoldAndQueueGateTest {

    /** 콜백을 그대로 실행하는 TransactionTemplate. 트랜잭션 경계는 통합 테스트의 몫. */
    private static TransactionTemplate directTx() {
        TransactionTemplate tx = mock(TransactionTemplate.class);
        when(tx.execute(any())).thenAnswer(inv ->
                ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        return tx;
    }

    private static Reservation pendingReservation() {
        return Reservation.builder()
                .id(1L)
                .totalPrice(10_000)
                .status(ReservationStatus.PENDING_PAYMENT)
                .orderId("tck-1")
                .build();
    }

    @Test
    void 홀드가_만료된_예약은_취소되고_좌석이_반환된다() {
        Showtime showtime = Showtime.builder().id(1L).build();
        SeatInventory inventory = SeatInventory.builder()
                .id(10L)
                .showtime(showtime)
                .price(BigDecimal.valueOf(10000))
                .status(SeatStatus.RESERVED)
                .holdExpiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        Reservation reservation = Reservation.builder()
                .id(100L)
                .showtime(showtime)
                .totalPrice(10000)
                .status(ReservationStatus.PENDING_PAYMENT)
                .reservedAt(LocalDateTime.now().minusMinutes(30))
                .orderId("tck-test")
                .build();
        reservation.getReservationSeats().add(
                ReservationSeat.builder().reservation(reservation).seatInventory(inventory).build());

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByStatusAndReservedAtBefore(eq(ReservationStatus.PENDING_PAYMENT), any()))
                .thenReturn(List.of(reservation));

        TicketingService service = new TicketingService(
                null, null, null, reservationRepository, mock(SeatRealtimeService.class), null, null, directTx());

        service.releaseExpiredHolds();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(inventory.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(inventory.getHoldExpiresAt()).isNull();
    }

    @Test
    void 대기열_토큰이_없거나_허용되지_않으면_통과하지_못한다() {
        QueueRepository queueRepository = mock(QueueRepository.class);
        QueueService queueService = new QueueService(queueRepository);

        // 토큰 자체가 없는 경우
        assertThat(queueService.isReady(null)).isFalse();
        assertThat(queueService.isReady("  ")).isFalse();

        // 메타는 없는데 토큰만 지어낸 경우
        when(queueRepository.getTokenMeta("fake")).thenReturn(Map.of());
        assertThat(queueService.isReady("fake")).isFalse();

        // 아직 순서가 안 온 경우
        when(queueRepository.getTokenMeta("waiting")).thenReturn(Map.of("eventId", "1"));
        when(queueRepository.isReady(anyLong(), eq("waiting"))).thenReturn(false);
        assertThat(queueService.isReady("waiting")).isFalse();

        // 입장 허용된 경우
        when(queueRepository.getTokenMeta("ready")).thenReturn(Map.of("eventId", "1"));
        when(queueRepository.isReady(1L, "ready")).thenReturn(true);
        assertThat(queueService.isReady("ready")).isTrue();
    }

    @Test
    void 예매에_사용된_토큰은_소비되어_재사용할_수_없다() {
        QueueRepository queueRepository = mock(QueueRepository.class);
        QueueService queueService = new QueueService(queueRepository);

        when(queueRepository.getTokenMeta("ready")).thenReturn(Map.of("eventId", "1"));

        queueService.consume("ready");
        verify(queueRepository).removeReady(1L, "ready");

        // 메타가 없는 토큰은 조용히 무시 (Redis 호출 없음)
        when(queueRepository.getTokenMeta("fake")).thenReturn(Map.of());
        queueService.consume("fake");
        queueService.consume(null);
        verifyNoMoreInteractions(ignoreStubs(queueRepository));
    }

    @Test
    void 좌석_브로드캐스트는_커밋_이후에만_나간다() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        SeatRealtimeService service = new SeatRealtimeService(messagingTemplate);
        SeatInventory inventory = SeatInventory.builder().id(10L).status(SeatStatus.RESERVED).build();

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.broadcastSeatStatuses(1L, List.of(inventory));
            // 커밋 전: 롤백될 수도 있으므로 아직 나가면 안 된다
            verifyNoInteractions(messagingTemplate);

            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
            verify(messagingTemplate).convertAndSend(eq("/topic/seat/1"), any(SeatStatusMessage.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void 트랜잭션_밖에서는_즉시_브로드캐스트한다() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        SeatRealtimeService service = new SeatRealtimeService(messagingTemplate);

        service.broadcastSeatStatuses(1L, List.of(SeatInventory.builder().id(10L).build()));

        verify(messagingTemplate).convertAndSend(eq("/topic/seat/1"), any(SeatStatusMessage.class));
    }

    @Test
    void 토스가_결제를_거절하면_좌석_점유를_되돌린다() {
        Reservation reservation = pendingReservation();
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByOrderId("tck-1")).thenReturn(Optional.of(reservation));

        TossPaymentClient toss = mock(TossPaymentClient.class);
        when(toss.confirmPayment(any())).thenThrow(new IllegalStateException("토스 결제 승인 실패"));

        TicketingService service = new TicketingService(
                null, null, null, reservationRepository, mock(SeatRealtimeService.class), toss, null, directTx());

        assertThatThrownBy(() -> service.confirmPayment("tck-1", "pk_1", 10_000L))
                .isInstanceOf(IllegalStateException.class);

        // 승인이 안 된 것이 확실하므로 좌석은 다시 회수 대상이어야 한다
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(reservation.getPaymentKey()).isNull();
    }

    @Test
    void 승인_응답을_못_받으면_좌석을_잠근_채_남긴다() {
        Reservation reservation = pendingReservation();
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByOrderId("tck-1")).thenReturn(Optional.of(reservation));

        TossPaymentClient toss = mock(TossPaymentClient.class);
        when(toss.confirmPayment(any())).thenThrow(new ResourceAccessException("read timed out"));

        TicketingService service = new TicketingService(
                null, null, null, reservationRepository, mock(SeatRealtimeService.class), toss, null, directTx());

        assertThatThrownBy(() -> service.confirmPayment("tck-1", "pk_1", 10_000L))
                .isInstanceOf(ResourceAccessException.class);

        // 결제됐을 수도 있다 → 회수 대상(PENDING_PAYMENT)으로 되돌리면 안 된다
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING_CONFIRM);
        assertThat(reservation.getPaymentKey()).isEqualTo("pk_1");
    }

    @Test
    void 이미_결제된_주문은_다시_승인하지_않는다() {
        Reservation reservation = pendingReservation();
        reservation.setStatus(ReservationStatus.PAID);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        when(reservationRepository.findByOrderId("tck-1")).thenReturn(Optional.of(reservation));

        TossPaymentClient toss = mock(TossPaymentClient.class);
        TicketingService service = new TicketingService(
                null, null, null, reservationRepository, mock(SeatRealtimeService.class), toss, null, directTx());

        service.confirmPayment("tck-1", "pk_1", 10_000L);

        verifyNoInteractions(toss);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PAID);
    }
}

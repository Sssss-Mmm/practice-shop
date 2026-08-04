package com.example.practice_shop.service;

import com.example.practice_shop.dtos.ticketing.ReservationResponse;
import com.example.practice_shop.dtos.ticketing.SeatSelectionRequest;
import com.example.practice_shop.entity.Reservation;
import com.example.practice_shop.entity.ReservationSeat;
import com.example.practice_shop.constant.ReservationStatus;
import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.entity.SeatInventory;
import com.example.practice_shop.entity.Showtime;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.exception.CustomException;
import com.example.practice_shop.exception.ErrorCode;
import com.example.practice_shop.repository.ReservationRepository;
import com.example.practice_shop.repository.SeatInventoryRepository;
import com.example.practice_shop.repository.ShowtimeRepository;
import com.example.practice_shop.repository.UserRepository;
import com.example.practice_shop.repository.PaymentRepository; // Import Added
import com.example.practice_shop.integration.toss.TossPaymentClient; // Import Added

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketingService {

    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRealtimeService seatRealtimeService;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final TransactionTemplate transactionTemplate;

    /** 결제 대기 좌석을 점유해 두는 시간. 이 시간이 지나면 스케줄러가 회수한다. */
    private static final Duration HOLD_DURATION = Duration.ofMinutes(10);

    /**
     * 예약을 생성합니다.
     * @param email
     * @param request
     * @return
     */
    @Transactional
    public ReservationResponse createReservation(String email, SeatSelectionRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));

        List<SeatInventory> selectedInventories = seatInventoryRepository.findAllBySeatIdInAndShowtimeIdWithLock(request.getSeatIds(), showtime.getId());

        if (selectedInventories.size() != request.getSeatIds().size()) {
            throw new CustomException(ErrorCode.SEAT_NOT_FOUND);
        }

        // 좌석이 이미 예약되었는지 확인
        // JPA Dirty Checking: 영속성 컨텍스트에서 관리되는 엔티티는
        // 트랜잭션 커밋 시 자동으로 변경사항이 DB에 반영됩니다.
        LocalDateTime holdExpiresAt = LocalDateTime.now().plus(HOLD_DURATION);
        for (SeatInventory inventory : selectedInventories) {
            if (inventory.getStatus() != SeatStatus.AVAILABLE) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
            inventory.setStatus(SeatStatus.RESERVED); // 좌석 상태를 '예약됨'으로 변경
            inventory.setHoldExpiresAt(holdExpiresAt); // 결제 미완료 시 회수 기준 시각
        }
        // Dirty Checking으로 트랜잭션 종료 시 자동 반영되므로 saveAll() 불필요

        BigDecimal totalPrice = selectedInventories.stream()
                .map(inv -> inv.getSeat().getBasePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Reservation reservation = Reservation.builder()
                .user(user)
                .showtime(showtime)
                .totalPrice(totalPrice.intValue()) // BigDecimal을 int로 변환
                .status(ReservationStatus.PENDING_PAYMENT) // 결제 대기 상태로 생성
                .reservedAt(LocalDateTime.now())
                .orderId("tck-" + UUID.randomUUID()) // 결제를 위한 고유 ID 생성
                .build();

        List<ReservationSeat> reservationSeats = new ArrayList<>();
        for (SeatInventory inventory : selectedInventories) {
            reservationSeats.add(
                    ReservationSeat.builder()
                            .reservation(reservation)
                            .seatInventory(inventory)
                            .build()
            );
        }
        reservation.getReservationSeats().addAll(reservationSeats);

        Reservation savedReservation = reservationRepository.save(reservation);

        // WebSocket으로 좌석 상태 브로드캐스트
        seatRealtimeService.broadcastSeatStatuses(showtime.getId(), selectedInventories);

        return toResponse(savedReservation);
    }

    /**
     * 사용자의 예약 목록을 조회합니다.
     * @param email
     * @return
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(String email) {
        List<Reservation> reservations = reservationRepository.findByUser_Email(email);
        return reservations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 예약을 취소합니다.
     * <p>
     * 1. 예약 정보를 조회하고 이미 취소된 상태인지 확인합니다.
     * 2. 결제가 완료된 예약(PAID)인 경우, Toss Payments API를 호출하여 전액 환불을 진행합니다.
     * 3. 예약 상태를 CANCELLED로 변경하고, 점유했던 좌석들을 다시 AVAILABLE 상태로 반환합니다.
     * </p>
     * @param email 사용자 이메일
     * @param reservationId 취소할 예약 ID
     */
    @Transactional
    public void cancelReservation(String email, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUser_Email(reservationId, email)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED_RESERVATION);
        }

        // 승인 결과를 모르는 상태에서 취소하면 결제됐을지 모르는 좌석을 환불 없이 풀어주게 된다.
        if (reservation.getStatus() == ReservationStatus.PENDING_CONFIRM) {
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_IN_PROGRESS);
        }

        // 결제 완료 상태라면 환불 처리
        if (reservation.getStatus() == ReservationStatus.PAID && reservation.getPaymentKey() != null) {
            // Toss Payments 결제 취소 API 호출
            tossPaymentClient.cancelPayment(reservation.getPaymentKey(), "사용자 요청에 의한 취소");
            
            // Payment 엔티티 상태 업데이트 (옵션: 환불 이력 저장)
            paymentRepository.findByReservation(reservation).ifPresent(p -> {
                 p.setStatus(com.example.practice_shop.constant.PaymentStatus.REFUNDED);
                 paymentRepository.save(p);
            });
        }

        releaseReservation(reservation);
    }

    /**
     * 결제되지 않은 채 홀드 시간이 지난 예약을 회수합니다.
     * <p>
     * 결제창에서 이탈한 사용자가 좌석을 영구 점유하는 것을 막습니다.
     * </p>
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void releaseExpiredHolds() {
        LocalDateTime cutoff = LocalDateTime.now().minus(HOLD_DURATION);
        List<Reservation> expired = reservationRepository
                .findByStatusAndReservedAtBefore(ReservationStatus.PENDING_PAYMENT, cutoff);

        expired.forEach(this::releaseReservation);
    }

    /**
     * 예약을 취소 상태로 바꾸고 점유 중이던 좌석을 모두 반환합니다.
     * @param reservation 대상 예약
     */
    private void releaseReservation(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
        // Dirty Checking: 트랜잭션 커밋 시 자동 반영

        // 좌석 상태 변경 (Dirty Checking으로 자동 반영)
        List<SeatInventory> inventoriesToRelease = new ArrayList<>();
        for (ReservationSeat reservationSeat : reservation.getReservationSeats()) {
            SeatInventory inventory = reservationSeat.getSeatInventory();
            inventory.setStatus(SeatStatus.AVAILABLE); // 좌석 상태를 '사용 가능'으로 변경
            inventory.setHoldExpiresAt(null); // 점유 해제
            inventoriesToRelease.add(inventory);
        }
        // saveAll() 불필요: JPA Dirty Checking으로 트랜잭션 커밋 시 자동 반영

        // WebSocket으로 좌석 상태 브로드캐스트
        seatRealtimeService.broadcastSeatStatuses(reservation.getShowtime().getId(), inventoriesToRelease);
    }

    /**
     * 결제 승인 및 예매 확정.
     * <p>
     * 외부 승인(Toss)과 DB 쓰기는 원자적으로 묶을 수 없다. 하나의 트랜잭션 안에서 승인을 호출하면
     * 승인 성공 후 커밋이 실패했을 때 돈만 빠져나가고, 그 예약은 PENDING_PAYMENT로 남아
     * {@link #releaseExpiredHolds()}에 회수되어 좌석까지 남에게 다시 팔린다.
     * <p>
     * 그래서 승인 시도를 먼저 커밋(PENDING_CONFIRM)한 뒤 승인을 호출한다. 회수 스케줄러는
     * PENDING_PAYMENT만 대상으로 하므로, 이후 어느 단계에서 죽더라도 좌석은 잠긴 채 남는다.
     *
     * @param orderId 주문 ID
     * @param paymentKey 결제 키
     * @param amount 결제 금액
     */
    public void confirmPayment(String orderId, String paymentKey, Long amount) {
        // 1) 승인을 시도한다는 사실부터 커밋한다.
        Boolean proceed = transactionTemplate.execute(status -> beginConfirm(orderId, paymentKey, amount));
        if (!Boolean.TRUE.equals(proceed)) {
            return; // 이미 확정된 주문 — 재요청은 조용히 무시한다 (멱등)
        }

        // 2) 외부 승인. 트랜잭션 밖이라 DB 커넥션을 물고 외부 응답을 기다리지 않는다.
        com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest confirmRequest = new com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest();
        confirmRequest.setPaymentKey(paymentKey);
        confirmRequest.setOrderId(orderId);
        confirmRequest.setAmount(amount);

        try {
            tossPaymentClient.confirmPayment(confirmRequest);
        } catch (ResourceAccessException e) {
            // 타임아웃·네트워크 오류: 승인됐는지 알 수 없다. PENDING_CONFIRM으로 남겨 좌석을 잠가 둔다.
            // 결제됐을지 모르는 좌석을 남에게 다시 파는 것보다, 좌석이 묶인 채 사람이 확인하는 편이 낫다.
            // ponytail: 자동 대사(reconcile) 없음. Toss 결제 조회 API를 붙여 PENDING_CONFIRM 건을
            //           주기적으로 확정/해제하는 스케줄러가 다음 단계.
            throw e;
        } catch (RuntimeException e) {
            // 토스가 명시적으로 거절했다 = 승인되지 않은 것이 확실하다. 좌석을 즉시 되돌린다.
            transactionTemplate.execute(status -> {
                revertConfirm(orderId);
                return null;
            });
            throw e;
        }

        // 3) 확정.
        transactionTemplate.execute(status -> {
            finalizeConfirm(orderId, paymentKey, amount);
            return null;
        });
    }

    /**
     * 승인 요청 직전 상태를 커밋합니다.
     * @return 승인을 진행해야 하면 true, 이미 확정된 주문이면 false
     */
    private boolean beginConfirm(String orderId, String paymentKey, Long amount) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.PAID) {
            return false;
        }
        if (reservation.getTotalPrice() != amount) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        reservation.setStatus(ReservationStatus.PENDING_CONFIRM);
        reservation.setPaymentKey(paymentKey);
        return true;
    }

    /** 승인이 거절됐을 때 좌석을 다시 회수 대상으로 되돌립니다. */
    private void revertConfirm(String orderId) {
        reservationRepository.findByOrderId(orderId).ifPresent(reservation -> {
            reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
            reservation.setPaymentKey(null);
        });
    }

    /** 승인 성공 후 예매를 확정합니다. */
    private void finalizeConfirm(String orderId, String paymentKey, Long amount) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.setStatus(ReservationStatus.PAID);
        reservation.setPaymentKey(paymentKey);

        // 좌석을 판매 완료로 확정하고 홀드를 해제한다.
        List<SeatInventory> soldInventories = new ArrayList<>();
        for (ReservationSeat reservationSeat : reservation.getReservationSeats()) {
            SeatInventory inventory = reservationSeat.getSeatInventory();
            inventory.setStatus(SeatStatus.SOLD);
            inventory.setHoldExpiresAt(null);
            soldInventories.add(inventory);
        }
        seatRealtimeService.broadcastSeatStatuses(reservation.getShowtime().getId(), soldInventories);

        // 누적 결제 금액 반영 → 등급 자동 승급 (User.accumulateSpend)
        reservation.getUser().accumulateSpend(amount);

        com.example.practice_shop.entity.Payment payment = com.example.practice_shop.entity.Payment.builder()
                .reservation(reservation)
                .amount(BigDecimal.valueOf(amount))
                .status(com.example.practice_shop.constant.PaymentStatus.PAID)
                .paymentMethod("CARD") // 상세 정보는 응답에서 가져와야 하지만 여기서는 간소화
                .paymentGateway("TOSS")
                .paymentKey(paymentKey)
                .build();
        paymentRepository.save(payment);
    }

    /**
     * Reservation 엔티티를 ReservationResponse DTO로 변환합니다.
     * @param reservation
     * @return
     */
    private ReservationResponse toResponse(Reservation reservation) {
        Showtime showtime = reservation.getShowtime();
        List<String> seatDetails = reservation.getReservationSeats().stream()
                .map(rs -> {
                    var seat = rs.getSeatInventory().getSeat();
                    return String.format("%s %s (%s)", seat.getRowLabel(), seat.getSeatNumber(), seat.getSeatType());
                })
                .collect(Collectors.toList());

        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .eventName(showtime.getEvent().getTitle())
                .venueName(showtime.getEvent().getVenue().getName())
                .showtime(showtime.getStartDateTime())
                .seatDetails(seatDetails)
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .reservedAt(reservation.getReservedAt())
                .orderId(reservation.getOrderId())
                .build();
    }
}
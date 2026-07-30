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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketingServiceImpl implements TicketingService {

    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRealtimeService seatRealtimeService;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;

    /**
     * 예약을 생성합니다.
     * @param email
     * @param request
     * @return
     */
    @Override
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
        for (SeatInventory inventory : selectedInventories) {
            if (inventory.getStatus() != SeatStatus.AVAILABLE) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
            inventory.setStatus(SeatStatus.RESERVED); // 좌석 상태를 '예약됨'으로 변경
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
    @Override
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
    @Override
    @Transactional
    public void cancelReservation(String email, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUser_Email(reservationId, email)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED_RESERVATION);
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

        reservation.setStatus(ReservationStatus.CANCELLED);
        // Dirty Checking: 트랜잭션 커밋 시 자동 반영

        // 좌석 상태 변경 (Dirty Checking으로 자동 반영)
        List<SeatInventory> inventoriesToRelease = new ArrayList<>();
        for (ReservationSeat reservationSeat : reservation.getReservationSeats()) {
            SeatInventory inventory = reservationSeat.getSeatInventory();
            inventory.setStatus(SeatStatus.AVAILABLE); // 좌석 상태를 '사용 가능'으로 변경
            inventoriesToRelease.add(inventory);
        }
        // saveAll() 불필요: JPA Dirty Checking으로 트랜잭션 커밋 시 자동 반영

        // WebSocket으로 좌석 상태 브로드캐스트
        seatRealtimeService.broadcastSeatStatuses(reservation.getShowtime().getId(), inventoriesToRelease);
    }

    /**
     * 결제 승인 및 예매 확정.
     * <p>
     * 프론트엔드에서 결제 승인 성공 후 호출됩니다.
     * 1. 주문 ID로 예약 정보를 조회하고 결제 금액을 검증합니다.
     * 2. Toss Payments에 최종 승인 요청을 보냅니다.
     * 3. 예약 상태를 PAID로 변경하고 Payment 엔티티를 생성하여 저장합니다.
     * </p>
     * @param orderId 주문 ID
     * @param paymentKey 결제 키
     * @param amount 결제 금액
     */
    @Override
    @Transactional
    public void confirmPayment(String orderId, String paymentKey, Long amount) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        
        if (reservation.getTotalPrice() != amount) {
             throw new CustomException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        // 토스 결제 승인 요청
        com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest confirmRequest = new com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest();
        confirmRequest.setPaymentKey(paymentKey);
        confirmRequest.setOrderId(orderId);
        confirmRequest.setAmount(amount);
        
        tossPaymentClient.confirmPayment(confirmRequest);

        // 예매 상태 업데이트
        reservation.setStatus(ReservationStatus.PAID);
        reservation.setPaymentKey(paymentKey);
        reservationRepository.save(reservation);

        // Payment 엔티티 생성 및 저장
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
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

    @Override
    @Transactional
    public ReservationResponse createReservation(String email, SeatSelectionRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHOWTIME_NOT_FOUND));

        List<SeatInventory> selectedInventories = seatInventoryRepository.findAllByIdInAndShowtimeIdWithLock(request.getSeatIds(), showtime.getId());

        if (selectedInventories.size() != request.getSeatIds().size()) {
            throw new CustomException(ErrorCode.SEAT_NOT_FOUND);
        }

        // 좌석이 이미 예약되었는지 확인
        for (SeatInventory inventory : selectedInventories) {
            if (inventory.getStatus() != SeatStatus.AVAILABLE) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
            inventory.setStatus(SeatStatus.RESERVED); // 좌석 상태를 '예약됨'으로 변경
        }
        seatInventoryRepository.saveAll(selectedInventories);

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

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(String email) {
        List<Reservation> reservations = reservationRepository.findByUser_Email(email);
        return reservations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelReservation(String email, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUser_Email(reservationId, email)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED_RESERVATION);
        }

        // TODO: 결제 취소 로직 추가 (Toss Payments API 연동 등)

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);


        List<SeatInventory> inventoriesToRelease = new ArrayList<>();
        for (ReservationSeat reservationSeat : reservation.getReservationSeats()) {
            SeatInventory inventory = reservationSeat.getSeatInventory();
            inventory.setStatus(SeatStatus.AVAILABLE); // 좌석 상태를 '사용 가능'으로 변경
            inventoriesToRelease.add(inventory);
        }
        seatInventoryRepository.saveAll(inventoriesToRelease);

        // WebSocket으로 좌석 상태 브로드캐스트
        seatRealtimeService.broadcastSeatStatuses(reservation.getShowtime().getId(), inventoriesToRelease);
    }

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
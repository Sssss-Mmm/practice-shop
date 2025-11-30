package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.ticketing.ReservationResponse;
import com.example.practice_shop.dtos.ticketing.SeatSelectionRequest;
import com.example.practice_shop.service.TicketingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticketing")
@RequiredArgsConstructor
public class TicketingController {

    private final TicketingService ticketingService;

    /**
     * 좌석 선택 및 예매 생성
     * @param authentication
     * @param request
     * @return
     */
    @PostMapping("/reserve")
    @Operation(summary = "좌석 선택 및 예매 생성", description = "선택한 좌석으로 예매를 생성하고 결제 전 단계까지 진행합니다.")
    public ResponseEntity<ReservationResponse> reserveSeats(Authentication authentication,
                                                            @Valid @RequestBody SeatSelectionRequest request) {
        String email = authentication.getName();
        ReservationResponse reservation = ticketingService.createReservation(email, request);
        return ResponseEntity.ok(reservation);
    }

    /**
     * 내 예매 내역 조회
     * @param authentication
     * @return
     */
    @GetMapping("/reservations")
    @Operation(summary = "내 예매 내역 조회", description = "사용자의 모든 예매 내역을 조회합니다.")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(ticketingService.getUserReservations(email));
    }

    /**
     * 예매 취소
     * @param authentication
     * @param reservationId
     * @return
     */
    @PostMapping("/reservations/{reservationId}/cancel")
    @Operation(summary = "예매 취소", description = "특정 예매 건을 취소합니다.")
    public ResponseEntity<Void> cancelReservation(Authentication authentication, @PathVariable Long reservationId) {
        String email = authentication.getName();
        ticketingService.cancelReservation(email, reservationId);
        return ResponseEntity.noContent().build();
    }
}
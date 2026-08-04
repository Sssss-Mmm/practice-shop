package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.ticketing.ReservationResponse;
import com.example.practice_shop.dtos.ticketing.SeatSelectionRequest;
import com.example.practice_shop.service.QueueService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticketing")
@RequiredArgsConstructor
public class TicketingController {

    private final TicketingService ticketingService;
    private final QueueService queueService;

    /**
     * 좌석 선택 및 예매 생성
     * @param authentication
     * @param queueToken 대기열 통과 토큰 (Queue-Token 헤더)
     * @param request
     * @return
     */
    @PostMapping("/reserve")
    @Operation(summary = "좌석 선택 및 예매 생성", description = "선택한 좌석으로 예매를 생성하고 결제 전 단계까지 진행합니다.")
    public ResponseEntity<ReservationResponse> reserveSeats(Authentication authentication,
                                                            @RequestHeader(value = "Queue-Token", required = false) String queueToken,
                                                            @Valid @RequestBody SeatSelectionRequest request) {
        // 대기열을 통과한 토큰만 예매를 허용한다. (허용 토큰 TTL 5분)
        if (!queueService.isReady(queueToken)) {
            throw new IllegalArgumentException("대기열 입장 토큰이 없거나 만료되었습니다. 대기열에 다시 진입해 주세요.");
        }

        String email = authentication.getName();
        ReservationResponse reservation = ticketingService.createReservation(email, request);

        // 예매가 성사된 뒤에만 토큰을 소비한다. (좌석 선점 실패 시에는 재시도 가능)
        queueService.consume(queueToken);

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
package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.ticketing.SeatStatusMessage;
import com.example.practice_shop.repository.SeatInventoryRepository;
import com.example.practice_shop.service.SeatRealtimeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/showtimes/{showtimeId}/seats/realtime")
@RequiredArgsConstructor
public class SeatRealtimeController {

    private final SeatInventoryRepository seatInventoryRepository;
    private final SeatRealtimeService seatRealtimeService;

    /**
     * 좌석 상태 브로드캐스트
     * @param showtimeId
     * @return
     */
    /**
     * 회차의 현재 좌석 상태 (좌석 화면 첫 로딩용)
     * <p>공연장 좌석 목록에는 회차별 예매 상태가 없어서, 이미 팔린 좌석도 처음엔 '선택 가능'으로 보였다.</p>
     */
    @GetMapping
    @Operation(summary = "회차 좌석 상태 조회", description = "회차의 좌석별 현재 상태(선택 가능/예매됨 등)를 반환합니다.")
    public ResponseEntity<List<SeatStatusMessage.SeatStatusItem>> current(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(seatRealtimeService.snapshot(seatInventoryRepository.findByShowtimeId(showtimeId)));
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "좌석 상태 브로드캐스트", description = "해당 회차의 좌석 상태를 WebSocket으로 전송합니다.")
    public ResponseEntity<Void> broadcast(@PathVariable Long showtimeId) {
        seatRealtimeService.broadcastSeatStatuses(showtimeId, seatInventoryRepository.findByShowtimeId(showtimeId));
        return ResponseEntity.ok().build();
    }
}

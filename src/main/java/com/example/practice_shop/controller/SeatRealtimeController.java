package com.example.practice_shop.controller;

import com.example.practice_shop.repository.SeatInventoryRepository;
import com.example.practice_shop.service.SeatRealtimeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "좌석 상태 브로드캐스트", description = "해당 회차의 좌석 상태를 WebSocket으로 전송합니다.")
    public ResponseEntity<Void> broadcast(@PathVariable Long showtimeId) {
        var inventories = seatInventoryRepository.findAll().stream()
                .filter(inv -> inv.getShowtime() != null && showtimeId.equals(inv.getShowtime().getId()))
                .toList();
        seatRealtimeService.broadcastSeatStatuses(showtimeId, inventories);
        return ResponseEntity.ok().build();
    }
}

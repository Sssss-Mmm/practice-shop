package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.queue.QueueEnterResponse;
import com.example.practice_shop.dtos.queue.QueueStatusResponse;
import com.example.practice_shop.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    /**
     * 대기열 진입/상태 조회 API 컨트롤러.
     * 프론트는 enter로 토큰을 받고, status를 폴링하며 ready 여부를 확인한다.
     */
    private final QueueService queueService;

    @PostMapping("/enter/{eventId}")
    @Operation(summary = "대기열 진입", description = "대기열 토큰을 발급하고 현재 순번을 반환합니다.")
    public ResponseEntity<QueueEnterResponse> enter(@PathVariable Long eventId, Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(queueService.enter(eventId, userId));
    }

    @GetMapping("/status")
    @Operation(summary = "대기열 상태 조회", description = "토큰으로 대기열 순번과 입장 가능 여부를 확인합니다.")
    public ResponseEntity<QueueStatusResponse> status(@RequestParam String token) {
        return ResponseEntity.ok(queueService.status(token));
    }
}

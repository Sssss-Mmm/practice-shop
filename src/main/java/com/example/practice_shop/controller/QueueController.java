package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.queue.QueueEnterResponse;
import com.example.practice_shop.dtos.queue.QueueStatusResponse;
import com.example.practice_shop.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import com.example.practice_shop.exception.CustomException;
import com.example.practice_shop.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * 부하 테스트 전용. true일 때만 비로그인 요청이 ?userId=로 진입할 수 있다.
     * 운영에서 켜면 아무나 남의 이름으로 토큰을 무한정 받을 수 있으므로 기본값은 false.
     */
    @Value("${queue.load-test-mode:false}")
    private boolean loadTestMode;

    @PostMapping("/enter/{eventId}")
    @Operation(summary = "대기열 진입", description = "대기열 토큰을 발급하고 현재 순번을 반환합니다.")
    public ResponseEntity<QueueEnterResponse> enter(@PathVariable Long eventId, @RequestParam(required = false) String userId, Authentication authentication) {
        String owner;
        if (authentication != null && authentication.isAuthenticated()) {
            owner = authentication.getName();
        } else if (loadTestMode && userId != null) {
            owner = "loadtest:" + userId; // 실제 계정 이메일과 겹치지 않게 접두어
        } else {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        return ResponseEntity.ok(queueService.enter(eventId, owner));
    }

    @GetMapping("/status")
    @Operation(summary = "대기열 상태 조회", description = "토큰으로 대기열 순번과 입장 가능 여부를 확인합니다.")
    public ResponseEntity<QueueStatusResponse> status(@RequestParam String token) {
        return ResponseEntity.ok(queueService.status(token));
    }
}

package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest;
import com.example.practice_shop.service.TicketingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final TicketingService ticketingService;

    /**
     * 토스 결제 승인
     * @param authentication
     * @param request
     * @return
     */
    @PostMapping("/toss/confirm")
    @Operation(summary = "토스 결제 승인", description = "토스 결제 성공 후 결제 내역을 검증하고 주문을 확정합니다.")
    public ResponseEntity<?> confirmTossPayment(Authentication authentication,
                                                            @Valid @RequestBody TossPaymentConfirmRequest request) {

        // ponytail: 티켓 결제만 존재. 상품/주문 결제 추가 시 prefix 분기 or 전략 재도입.
        if (!request.getOrderId().startsWith("tck-")) {
            throw new IllegalArgumentException("지원하지 않는 주문 유형입니다.");
        }

        ticketingService.confirmPayment(request.getOrderId(), request.getPaymentKey(), request.getAmount());
        return ResponseEntity.ok("Payment confirmed");
    }
}

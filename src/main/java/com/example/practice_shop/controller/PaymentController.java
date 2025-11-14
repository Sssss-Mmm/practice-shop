package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Order.OrderResponse;
import com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest;
import com.example.practice_shop.service.OrderService;
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

    private final OrderService orderService;

    @PostMapping("/toss/confirm")
    @Operation(summary = "토스 결제 승인", description = "토스 결제 성공 후 결제 내역을 검증하고 주문을 확정합니다.")
    public ResponseEntity<OrderResponse> confirmTossPayment(Authentication authentication,
                                                            @Valid @RequestBody TossPaymentConfirmRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(orderService.confirmTossPayment(email, request));
    }
}

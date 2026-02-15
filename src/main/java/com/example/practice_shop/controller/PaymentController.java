package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest;
import com.example.practice_shop.service.payment.PaymentProcessorStrategy;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
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

    private final List<PaymentProcessorStrategy> paymentProcessors;

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
        
        PaymentProcessorStrategy processor = paymentProcessors.stream()
                .filter(p -> p.supports(request.getOrderId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 주문 유형입니다."));
        
        processor.process(request);
        return ResponseEntity.ok("Payment confirmed");
    }
}

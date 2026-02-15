package com.example.practice_shop.service.payment;

import com.example.practice_shop.dtos.Payment.TossPaymentConfirmRequest;

public interface PaymentProcessorStrategy {
    /**
     * 해당 주문 ID를 처리할 수 있는지 확인합니다.
     * @param orderId 주문 ID
     * @return 처리 가능 여부
     */
    boolean supports(String orderId);

    /**
     * 결제 승인 요청을 처리합니다.
     * @param request 결제 승인 요청 정보
     */
    void process(TossPaymentConfirmRequest request);
}

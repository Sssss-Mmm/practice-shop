package com.example.practice_shop.dtos.Payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentConfirmRequest {

    @NotBlank(message = "결제 키는 필수입니다.")
    private String paymentKey;

    /**
     * 토스에 전달한 주문 ID (주문번호)
     */
    @NotBlank(message = "주문 번호가 필요합니다.")
    private String orderId;

    @NotNull(message = "결제 금액이 필요합니다.")
    @Positive(message = "결제 금액은 1 이상이어야 합니다.")
    private Long amount;
}

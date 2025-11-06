package com.example.practice_shop.dtos.Order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateRequest {

    @NotBlank(message = "수령인 이름을 입력해 주세요.")
    private String recipientName;

    @NotBlank(message = "연락처를 입력해 주세요.")
    private String contactNumber;

    @NotBlank(message = "배송지를 입력해 주세요.")
    private String shippingAddress;

    private String postalCode;
    private String deliveryInstructions;

    @NotBlank(message = "결제 수단을 선택해 주세요.")
    private String paymentMethod;
}

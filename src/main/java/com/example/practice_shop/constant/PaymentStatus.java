package com.example.practice_shop.constant;

public enum PaymentStatus {
    PENDING("결제 대기"),
    PAID("결제 완료"),
    FAILED("결제 실패"),
    REFUNDED("환불 완료");

    public final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}

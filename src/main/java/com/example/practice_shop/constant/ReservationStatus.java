package com.example.practice_shop.constant;

public enum ReservationStatus {
    PENDING_PAYMENT, // 결제 대기 중
    PENDING_CONFIRM, // 외부 결제 승인 요청 보냄 — 결과 확인 전. 이 상태의 좌석은 자동 회수하지 않는다.
    CONFIRMED,       // 예매 확정
    PAID,            // 결제 완료
    CANCELLED        // 예매 취소
}
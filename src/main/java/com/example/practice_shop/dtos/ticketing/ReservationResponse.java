package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {

    private Long reservationId;

    private String eventName;

    private String venueName;

    private LocalDateTime showtime;

    private List<String> seatDetails; // 예: ["A열 1번 (VIP)", "A열 2번 (VIP)"]

    private int totalPrice;

    private ReservationStatus status;

    private LocalDateTime reservedAt;

    private String orderId; // 토스페이먼츠 연동을 위한 주문 ID
}
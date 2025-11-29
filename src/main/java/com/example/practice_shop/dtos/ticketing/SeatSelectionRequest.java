package com.example.practice_shop.dtos.ticketing;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SeatSelectionRequest {
    @NotNull(message = "회차 ID는 필수입니다.")
    private Long showtimeId;

    @NotEmpty(message = "좌석을 하나 이상 선택해야 합니다.")
    private List<Long> seatIds;
}
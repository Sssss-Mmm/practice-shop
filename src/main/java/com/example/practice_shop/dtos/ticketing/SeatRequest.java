package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.SeatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatRequest {

    @NotNull(message = "공연장 ID는 필수입니다.")
    private Long venueId;

    private String sectionName;

    private String rowLabel;

    @NotBlank(message = "좌석 번호는 필수입니다.")
    private String seatNumber;

    private String seatType;

    @NotNull(message = "기본 가격은 필수입니다.")
    @Positive(message = "가격은 0보다 커야 합니다.")
    private BigDecimal basePrice;

    private SeatStatus status;
}

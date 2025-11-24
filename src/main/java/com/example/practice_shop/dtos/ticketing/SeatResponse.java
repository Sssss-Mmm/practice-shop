package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.SeatStatus;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatResponse {
    private Long seatId;
    private Long venueId;
    private String sectionName;
    private String rowLabel;
    private String seatNumber;
    private String seatType;
    private BigDecimal basePrice;
    private SeatStatus status;
}

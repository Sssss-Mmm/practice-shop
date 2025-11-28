package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.SeatStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatStatusMessage {
    private Long showtimeId;
    private List<SeatStatusItem> seats;

    @Getter
    @Builder
    public static class SeatStatusItem {
        private Long seatInventoryId;
        private Long seatId;
        private String sectionName;
        private String rowLabel;
        private String seatNumber;
        private SeatStatus status;
        private LocalDateTime holdExpiresAt;
    }
}

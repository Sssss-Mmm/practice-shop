package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.ShowtimeStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShowtimeResponse {
    private Long showtimeId;
    private Long eventId;
    private Long venueId;
    private String eventTitle;
    private String venueName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime salesOpenAt;
    private LocalDateTime salesCloseAt;
    private Integer capacity;
    private ShowtimeStatus status;
}

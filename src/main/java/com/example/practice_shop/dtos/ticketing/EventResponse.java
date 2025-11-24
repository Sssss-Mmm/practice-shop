package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.EventStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventResponse {
    private Long eventId;
    private String title;
    private String description;
    private String category;
    private String organizerName;
    private String ageRestriction;
    private LocalDate salesStartDate;
    private LocalDate salesEndDate;
    private String posterImageUrl;
    private EventStatus status;
    private Long venueId;
    private String venueName;
}

package com.example.practice_shop.dtos.ticketing;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VenueResponse {
    private Long venueId;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String seatingChartUrl;
    private String description;
}

package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.EventStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequest {

    @NotBlank(message = "공연 제목은 필수입니다.")
    private String title;

    private String description;
    private String category;
    private String organizerName;
    private String ageRestriction;

    @FutureOrPresent(message = "판매 시작일은 오늘 이후여야 합니다.")
    private LocalDate salesStartDate;

    private LocalDate salesEndDate;
    private String posterImageUrl;

    @NotNull(message = "공연장 ID는 필수입니다.")
    private Long venueId;

    private EventStatus status;
}

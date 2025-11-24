package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.ShowtimeStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShowtimeRequest {

    @NotNull(message = "공연 ID는 필수입니다.")
    private Long eventId;

    @NotNull(message = "공연장 ID는 필수입니다.")
    private Long venueId;

    @NotNull(message = "시작 일시는 필수입니다.")
    @Future(message = "시작 일시는 미래여야 합니다.")
    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;
    private LocalDateTime salesOpenAt;
    private LocalDateTime salesCloseAt;
    private Integer capacity;
    private ShowtimeStatus status;
}

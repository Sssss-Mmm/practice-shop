package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.EventStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * 공연(Event) 생성 및 수정을 위한 요청 데이터를 담는 DTO 클래스입니다.
 * 클라이언트로부터 받은 JSON 데이터를 이 객체로 변환하여 사용합니다.
 */
@Getter
@Setter
public class EventRequest {

    /**
     * 공연의 제목입니다.
     * @NotBlank: null이 아니어야 하고, 하나 이상의 공백이 아닌 문자를 포함해야 합니다.
     */
    @NotBlank(message = "공연 제목은 필수입니다.")
    private String title;

    /**
     * 공연에 대한 상세 설명입니다.
     */
    private String description;

    /**
     * 공연의 카테고리입니다. (예: 콘서트, 뮤지컬, 연극)
     */
    private String category;

    /**
     * 공연 주최자의 이름입니다.
     */
    private String organizerName;

    /**
     * 관람 가능한 연령 제한 정보입니다. (예: "전체관람가", "15세 이상")
     */
    private String ageRestriction;

    /**
     * 티켓 판매 시작일입니다.
     * @FutureOrPresent: 날짜가 현재 또는 미래여야 합니다.
     */
    @FutureOrPresent(message = "판매 시작일은 오늘 이후여야 합니다.")
    private LocalDate salesStartDate;

    /**
     * 티켓 판매 종료일입니다.
     */
    private LocalDate salesEndDate;

    /**
     * 공연 포스터 이미지의 URL입니다.
     */
    private String posterImageUrl;

    /**
     * 공연이 열리는 공연장의 ID입니다.
     * @NotNull: null 값이 아니어야 합니다.
     */
    @NotNull(message = "공연장 ID는 필수입니다.")
    private Long venueId;

    /**
     * 공연의 상태를 나타냅니다. (예: UPCOMING, ONGOING, FINISHED)
     */
    private EventStatus status;
}

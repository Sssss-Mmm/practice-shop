package com.example.practice_shop.dtos.ticketing;

import com.example.practice_shop.constant.EventStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventResponse {
    private Long eventId; // 이벤트 ID
    private String title; // 이벤트 제목
    private String description; // 이벤트 설명
    private String category; // 이벤트 카테고리
    private String organizerName; // 이벤트 주최자
    private String ageRestriction; // 이벤트 연령제한
    private LocalDate salesStartDate; // 이벤트 판매시작일
    private LocalDate salesEndDate; // 이벤트 판매종료일
    private String posterImageUrl; // 이벤트 포스터 이미지 URL
    private EventStatus status; // 이벤트 상태
    private Long venueId; // 이벤트 장소 ID
    private String venueName; // 이벤트 장소 이름
    private VenueResponse venue; // 이벤트 장소
    private java.util.List<ShowtimeResponse> showtimes; // 이벤트 회차
}

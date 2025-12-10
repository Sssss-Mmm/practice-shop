package com.example.practice_shop.service;

import com.example.practice_shop.constant.ShowtimeStatus;
import com.example.practice_shop.dtos.ticketing.ShowtimeRequest;
import com.example.practice_shop.dtos.ticketing.ShowtimeResponse;
import com.example.practice_shop.entity.Event;
import com.example.practice_shop.entity.Showtime;
import com.example.practice_shop.entity.Venue;
import com.example.practice_shop.repository.EventRepository;
import com.example.practice_shop.repository.ShowtimeRepository;
import com.example.practice_shop.repository.VenueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final com.example.practice_shop.repository.SeatRepository seatRepository;
    private final com.example.practice_shop.repository.SeatInventoryRepository seatInventoryRepository;

    /**
     * 새 회차 생성
     * @param request
     * @return
     */
    @Transactional
    public ShowtimeResponse create(ShowtimeRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("공연을 찾을 수 없습니다."));
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("공연장을 찾을 수 없습니다."));

        ShowtimeStatus status = request.getStatus() != null ? request.getStatus() : ShowtimeStatus.SCHEDULED;

        Showtime showtime = Showtime.builder()
                .event(event)
                .venue(venue)
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .salesOpenAt(request.getSalesOpenAt())
                .salesCloseAt(request.getSalesCloseAt())
                .capacity(request.getCapacity())
                .status(status)
                .build();

        Showtime savedShowtime = showtimeRepository.save(showtime);

        // 좌석 재고 생성
        List<com.example.practice_shop.entity.Seat> seats = seatRepository.findByVenue(venue);
        List<com.example.practice_shop.entity.SeatInventory> inventories = seats.stream()
                .map(seat -> com.example.practice_shop.entity.SeatInventory.builder()
                        .showtime(savedShowtime)
                        .seat(seat)
                        .price(seat.getBasePrice())
                        .status(com.example.practice_shop.constant.SeatStatus.AVAILABLE)
                        .build())
                .collect(java.util.stream.Collectors.toList());
        
        seatInventoryRepository.saveAll(inventories);

        return toResponse(savedShowtime);
    }
    /**
     * 회차 조회
     * @param showtimeId
     * @return
     */
    @Transactional(readOnly = true)
    public ShowtimeResponse get(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다."));
        return toResponse(showtime);
    }
    /**
     * 이벤트별 회차 목록 조회
     * @param eventId
     * @return
     */
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> listByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("공연을 찾을 수 없습니다."));
        return showtimeRepository.findByEventAndStartDateTimeAfter(event, event.getSalesStartDate() != null ? event.getSalesStartDate().atStartOfDay() : null)
                .stream()
                .map(this::toResponse)
                .toList();
    }
    /**
     * Showtime 엔티티를 ShowtimeResponse DTO로 변환
     * @param showtime
     * @return
     */
    private ShowtimeResponse toResponse(Showtime showtime) {
        return ShowtimeResponse.builder()
                .showtimeId(showtime.getId())
                .eventId(showtime.getEvent() != null ? showtime.getEvent().getId() : null)
                .venueId(showtime.getVenue() != null ? showtime.getVenue().getId() : null)
                .eventTitle(showtime.getEvent() != null ? showtime.getEvent().getTitle() : null)
                .venueName(showtime.getVenue() != null ? showtime.getVenue().getName() : null)
                .startDateTime(showtime.getStartDateTime())
                .endDateTime(showtime.getEndDateTime())
                .salesOpenAt(showtime.getSalesOpenAt())
                .salesCloseAt(showtime.getSalesCloseAt())
                .capacity(showtime.getCapacity())
                .status(showtime.getStatus())
                .build();
    }
}

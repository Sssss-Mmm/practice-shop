package com.example.practice_shop.service;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.dtos.ticketing.EventRequest;
import com.example.practice_shop.dtos.ticketing.EventResponse;
import com.example.practice_shop.dtos.ticketing.ShowtimeResponse;
import com.example.practice_shop.entity.Event;
import com.example.practice_shop.entity.Venue;
import com.example.practice_shop.repository.EventRepository;
import com.example.practice_shop.repository.VenueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    /**
     * 새 공연 생성
     * @param request
     * @return
     */
    @Transactional
    public EventResponse create(EventRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("공연장을 찾을 수 없습니다."));

        EventStatus status = request.getStatus() != null ? request.getStatus() : EventStatus.DRAFT;

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .organizerName(request.getOrganizerName())
                .ageRestriction(request.getAgeRestriction())
                .salesStartDate(request.getSalesStartDate())
                .salesEndDate(request.getSalesEndDate())
                .posterImageUrl(request.getPosterImageUrl())
                .status(status)
                .venue(venue)
                .build();

        return toResponse(eventRepository.save(event));
    }
    
    /**
     * 공연 조회
     * @param eventId
     * @return
     */
    @Transactional(readOnly = true)
    public EventResponse get(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("공연을 찾을 수 없습니다."));
        return toResponse(event);
    }

    /**
     * 공연 목록 조회
     * @param status
     * @return
     */
    @Transactional(readOnly = true)
    public List<EventResponse> list(EventStatus status) {
        List<Event> events = (status == null) ? eventRepository.findAll() : eventRepository.findByStatus(status);
        return events.stream().map(this::toResponse).toList();
    }

    /**
     * Event 엔티티를 EventResponse DTO로 변환
     * @param event
     * @return
     */
    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .organizerName(event.getOrganizerName())
                .ageRestriction(event.getAgeRestriction())
                .salesStartDate(event.getSalesStartDate())
                .salesEndDate(event.getSalesEndDate())
                .posterImageUrl(event.getPosterImageUrl())
                .status(event.getStatus())
                .venueId(event.getVenue() != null ? event.getVenue().getId() : null)
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .showtimes(event.getShowtimes().stream().map(st -> ShowtimeResponse.builder()
                        .showtimeId(st.getId())
                        .eventId(st.getEvent().getId())
                        .venueId(st.getVenue().getId())
                        .eventTitle(st.getEvent().getTitle())
                        .venueName(st.getVenue().getName())
                        .startDateTime(st.getStartDateTime())
                        .endDateTime(st.getEndDateTime())
                        .salesOpenAt(st.getSalesOpenAt())
                        .salesCloseAt(st.getSalesCloseAt())
                        .capacity(st.getCapacity())
                        .status(st.getStatus())
                        .build()).toList())
                .build();
    }
}

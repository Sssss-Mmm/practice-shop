package com.example.practice_shop.service;

import com.example.practice_shop.dtos.ticketing.VenueRequest;
import com.example.practice_shop.dtos.ticketing.VenueResponse;
import com.example.practice_shop.entity.Venue;
import com.example.practice_shop.repository.VenueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueResponse create(VenueRequest request) {
        Venue venue = Venue.builder()
                .name(request.getName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .seatingChartUrl(request.getSeatingChartUrl())
                .description(request.getDescription())
                .build();

        return toResponse(venueRepository.save(venue));
    }

    @Transactional(readOnly = true)
    public VenueResponse get(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("공연장을 찾을 수 없습니다."));
        return toResponse(venue);
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> list() {
        return venueRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private VenueResponse toResponse(Venue venue) {
        return VenueResponse.builder()
                .venueId(venue.getId())
                .name(venue.getName())
                .addressLine1(venue.getAddressLine1())
                .addressLine2(venue.getAddressLine2())
                .city(venue.getCity())
                .state(venue.getState())
                .postalCode(venue.getPostalCode())
                .seatingChartUrl(venue.getSeatingChartUrl())
                .description(venue.getDescription())
                .build();
    }
}

package com.example.practice_shop.service;

import com.example.practice_shop.constant.SeatStatus;
import com.example.practice_shop.dtos.ticketing.SeatRequest;
import com.example.practice_shop.dtos.ticketing.SeatResponse;
import com.example.practice_shop.entity.Seat;
import com.example.practice_shop.entity.Venue;
import com.example.practice_shop.repository.SeatRepository;
import com.example.practice_shop.repository.VenueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;

    /**
     * 새 좌석 생성
     * @param request
     * @return
     */
    @Transactional
    public SeatResponse create(SeatRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("공연장을 찾을 수 없습니다."));

        SeatStatus status = request.getStatus() != null ? request.getStatus() : SeatStatus.AVAILABLE;

        Seat seat = buildSeat(venue, request, status);

        return toResponse(seatRepository.save(seat));
    }

    /**
     * 좌석 일괄 등록
     */
    @Transactional
    public List<SeatResponse> createBatch(List<SeatRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("등록할 좌석이 없습니다.");
        }
        Long venueId = requests.get(0).getVenueId();
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("공연장을 찾을 수 없습니다."));

        SeatStatus defaultStatus = SeatStatus.AVAILABLE;
        List<Seat> seats = requests.stream().map(req -> {
            if (!venueId.equals(req.getVenueId())) {
                throw new IllegalArgumentException("모든 좌석의 공연장 ID가 동일해야 합니다.");
            }
            SeatStatus status = req.getStatus() != null ? req.getStatus() : defaultStatus;
            return buildSeat(venue, req, status);
        }).toList();

        List<Seat> saved = seatRepository.saveAll(seats);
        return saved.stream().map(this::toResponse).toList();
    }

    /**
     * 공연장별 좌석 목록 조회
     * @param venueId
     * @return
     */
    @Transactional(readOnly = true)
    public List<SeatResponse> listByVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("공연장을 찾을 수 없습니다."));
        return seatRepository.findByVenue(venue).stream()
                .map(this::toResponse)
                .toList();
    }

    private Seat buildSeat(Venue venue, SeatRequest request, SeatStatus status) {
        return Seat.builder()
                .venue(venue)
                .sectionName(request.getSectionName())
                .rowLabel(request.getRowLabel())
                .seatNumber(request.getSeatNumber())
                .seatType(request.getSeatType())
                .basePrice(request.getBasePrice())
                .status(status)
                .build();
    }

    /**
     * Seat 엔티티를 SeatResponse DTO로 변환
     * @param seat
     * @return
     */
    private SeatResponse toResponse(Seat seat) {
        return SeatResponse.builder()
                .seatId(seat.getId())
                .venueId(seat.getVenue() != null ? seat.getVenue().getId() : null)
                .sectionName(seat.getSectionName())
                .rowLabel(seat.getRowLabel())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .basePrice(seat.getBasePrice())
                .status(seat.getStatus())
                .build();
    }
}

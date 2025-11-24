package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.ticketing.SeatRequest;
import com.example.practice_shop.dtos.ticketing.SeatResponse;
import com.example.practice_shop.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    @Operation(summary = "좌석 등록")
    public ResponseEntity<SeatResponse> create(@Valid @RequestBody SeatRequest request) {
        return ResponseEntity.ok(seatService.create(request));
    }

    @GetMapping("/venue/{venueId}")
    @Operation(summary = "공연장별 좌석 목록")
    public ResponseEntity<List<SeatResponse>> listByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(seatService.listByVenue(venueId));
    }
}

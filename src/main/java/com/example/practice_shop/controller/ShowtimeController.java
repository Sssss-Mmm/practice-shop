package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.ticketing.ShowtimeRequest;
import com.example.practice_shop.dtos.ticketing.ShowtimeResponse;
import com.example.practice_shop.service.ShowtimeService;
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
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PostMapping
    @Operation(summary = "회차 등록")
    public ResponseEntity<ShowtimeResponse> create(@Valid @RequestBody ShowtimeRequest request) {
        return ResponseEntity.ok(showtimeService.create(request));
    }

    @GetMapping("/{showtimeId}")
    @Operation(summary = "회차 상세 조회")
    public ResponseEntity<ShowtimeResponse> get(@PathVariable Long showtimeId) {
        return ResponseEntity.ok(showtimeService.get(showtimeId));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "공연별 회차 목록")
    public ResponseEntity<List<ShowtimeResponse>> listByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(showtimeService.listByEvent(eventId));
    }
}

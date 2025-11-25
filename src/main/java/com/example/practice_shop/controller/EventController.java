package com.example.practice_shop.controller;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.dtos.ticketing.EventRequest;
import com.example.practice_shop.dtos.ticketing.EventResponse;
import com.example.practice_shop.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * 공연 등록
     * @param request
     * @return
     */
    @PostMapping
    @Operation(summary = "공연 등록")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.create(request));
    }

    /**
     * 공연 목록 조회
     * @param status
     * @return
     */
    @GetMapping
    @Operation(summary = "공연 목록 조회")
    public ResponseEntity<List<EventResponse>> list(@RequestParam(required = false) EventStatus status) {
        return ResponseEntity.ok(eventService.list(status));
    }
    
    /**
     * 공연 상세 조회
     * @param eventId
     * @return
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "공연 상세 조회")
    public ResponseEntity<EventResponse> get(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.get(eventId));
    }
}

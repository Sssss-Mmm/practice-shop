package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.ticketing.VenueRequest;
import com.example.practice_shop.dtos.ticketing.VenueResponse;
import com.example.practice_shop.service.VenueService;
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
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    /** 
     * 공연장 등록
     * @param request
     * @return
     */
    @PostMapping
    @Operation(summary = "공연장 등록")
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.create(request));
    }

    /** 
     * 공연장 목록 조회
     * @return
     */
    @GetMapping
    @Operation(summary = "공연장 목록 조회")
    public ResponseEntity<List<VenueResponse>> list() {
        return ResponseEntity.ok(venueService.list());
    }
    
    /** 
     * 공연장 상세 조회
     * @param venueId
     * @return
     */
    @GetMapping("/{venueId}")
    @Operation(summary = "공연장 상세 조회")
    public ResponseEntity<VenueResponse> get(@PathVariable Long venueId) {
        return ResponseEntity.ok(venueService.get(venueId));
    }
}

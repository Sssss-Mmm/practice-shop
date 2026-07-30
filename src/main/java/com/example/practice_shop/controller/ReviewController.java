package com.example.practice_shop.controller;

import com.example.practice_shop.dtos.review.ReviewRequest;
import com.example.practice_shop.dtos.review.ReviewResponse;

import com.example.practice_shop.service.ReviewService;
import com.example.practice_shop.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping("/event/{eventId}")
    @Operation(summary = "리뷰 작성")
    public ResponseEntity<ReviewResponse> create(
            Authentication authentication,
            @PathVariable Long eventId,
            @Valid @RequestBody ReviewRequest request) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(reviewService.create(userId, eventId, request));
    }

    @GetMapping("/event/{eventId}")
    @Operation(summary = "리뷰 목록 조회")
    public ResponseEntity<List<ReviewResponse>> list(
            Authentication authentication,
            @PathVariable Long eventId) {
        Long userId = authentication != null ? getUserId(authentication) : null;
        return ResponseEntity.ok(reviewService.list(userId, eventId));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Long reviewId) {
        Long userId = getUserId(authentication);
        reviewService.delete(userId, reviewId);
        return ResponseEntity.ok().build();
    }

    private Long getUserId(Authentication authentication) {
        // JwtAuthenticationFilter가 principal에 이메일을 넣으므로 getName()이 곧 이메일이다.
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }
}

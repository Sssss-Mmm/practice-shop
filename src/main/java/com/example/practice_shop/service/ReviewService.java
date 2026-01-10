package com.example.practice_shop.service;

import com.example.practice_shop.dtos.review.ReviewRequest;
import com.example.practice_shop.dtos.review.ReviewResponse;
import com.example.practice_shop.entity.Event;
import com.example.practice_shop.entity.Review;
import com.example.practice_shop.entity.User;
import com.example.practice_shop.repository.EventRepository;
import com.example.practice_shop.repository.ReviewRepository;
import com.example.practice_shop.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse create(Long userId, Long eventId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("공연을 찾을 수 없습니다."));

        Review review = Review.builder()
                .user(user)
                .event(event)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        return toResponse(reviewRepository.save(review), userId);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> list(Long userId, Long eventId) {
        return reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(review -> toResponse(review, userId))
                .toList();
    }

    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review review, Long currentUserId) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName()) // Or nickname
                .eventId(review.getEvent().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .isOwner(currentUserId != null && currentUserId.equals(review.getUser().getId()))
                .build();
    }
}

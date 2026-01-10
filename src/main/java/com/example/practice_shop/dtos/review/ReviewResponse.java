package com.example.practice_shop.dtos.review;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long reviewId;
    private Long userId;
    private String userName;
    private Long eventId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private boolean isOwner; // To show delete button
}

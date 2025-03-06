package com.ll.hotel.domain.review.review.dto;

import com.ll.hotel.domain.review.review.entity.Review;

import java.time.LocalDateTime;

public record ReviewDto(
        Long reviewId,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    public ReviewDto(Review review) {
        this(review.getId(), review.getRating(), review.getContent(), review.getCreatedAt());
    }
}

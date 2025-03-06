package com.ll.hotel.domain.review.review.dto.response;

import com.ll.hotel.domain.review.comment.dto.ReviewCommentDto;
import com.ll.hotel.domain.review.comment.entity.ReviewComment;
import com.ll.hotel.domain.review.review.dto.ReviewDto;
import com.ll.hotel.domain.review.review.entity.Review;

import java.time.LocalDateTime;

public record HotelReviewWithCommentDto(
        String memberEmail,
        String roomTypeName,
        ReviewDto reviewDto,
        ReviewCommentDto reviewCommentDto,
        LocalDateTime createdAt
) {
    public HotelReviewWithCommentDto(String memberEmail, String roomTypeName, Review review, ReviewComment reviewComment, LocalDateTime createdAt) {
        this(
                memberEmail,
                roomTypeName,
                new ReviewDto(review),
                reviewComment != null ? new ReviewCommentDto(reviewComment) : null,
                createdAt
        );
    }
}

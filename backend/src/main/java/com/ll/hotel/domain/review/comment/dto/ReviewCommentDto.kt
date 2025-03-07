package com.ll.hotel.domain.review.comment.dto;

import com.ll.hotel.domain.review.comment.entity.ReviewComment;

import java.time.LocalDateTime;

public record ReviewCommentDto(
        long reviewCommentId,
        String content,
        LocalDateTime createdAt
) {
    public ReviewCommentDto(ReviewComment reviewComment){
        this(reviewComment.getId(), reviewComment.getContent(), reviewComment.getCreatedAt());
    }
}

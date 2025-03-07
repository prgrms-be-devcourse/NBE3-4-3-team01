package com.ll.hotel.domain.review.comment.dto;

import com.ll.hotel.domain.review.comment.entity.ReviewComment;

import java.time.LocalDateTime;

data class ReviewCommentDto(
    val reviewCommentId: Long,
    val content: String,
    val createdAt: LocalDateTime
) {
    constructor(reviewComment: ReviewComment) : this(
        reviewComment.id,
        reviewComment.content,
        reviewComment.createdAt
    );
}

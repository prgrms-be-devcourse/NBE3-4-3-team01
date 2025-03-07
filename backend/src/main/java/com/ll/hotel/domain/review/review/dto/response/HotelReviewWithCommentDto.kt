package com.ll.hotel.domain.review.review.dto.response;

import com.ll.hotel.domain.review.comment.dto.ReviewCommentDto;
import com.ll.hotel.domain.review.comment.entity.ReviewComment;
import com.ll.hotel.domain.review.review.dto.ReviewDto;
import com.ll.hotel.domain.review.review.entity.Review;

import java.time.LocalDateTime;

data class HotelReviewWithCommentDto(
    val memberEmail: String,
    val roomTypeName: String,
    val reviewDto: ReviewDto,
    val reviewCommentDto: ReviewCommentDto?,
    val createdAt: LocalDateTime
) {
    constructor(
        memberEmail: String,
        roomTypeName: String,
        review: Review,
        reviewComment: ReviewComment?,
        createdAt: LocalDateTime
    ) : this(
        memberEmail = memberEmail,
        roomTypeName = roomTypeName,
        reviewDto = ReviewDto(review),
        reviewComment?.let { ReviewCommentDto(it) },
        createdAt = createdAt
    )
}

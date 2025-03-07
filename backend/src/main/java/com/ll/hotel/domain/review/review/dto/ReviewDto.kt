package com.ll.hotel.domain.review.review.dto

import com.ll.hotel.domain.review.review.entity.Review
import java.time.LocalDateTime

data class ReviewDto(
    val reviewId: Long,
    val rating: Int,
    val content: String,
    val createdAt: LocalDateTime
) {
    constructor(review: Review) : this(review.id, review.rating, review.content, review.createdAt)
}

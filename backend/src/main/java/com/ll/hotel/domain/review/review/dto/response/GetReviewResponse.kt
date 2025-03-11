package com.ll.hotel.domain.review.review.dto.response;

import com.ll.hotel.domain.review.review.dto.ReviewDto;

data class GetReviewResponse(
    val reviewDto : ReviewDto,
    val imageUrls : List<String>
)
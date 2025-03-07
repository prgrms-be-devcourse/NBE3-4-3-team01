package com.ll.hotel.domain.review.review.dto.response;

data class HotelReviewResponse(
        val hotelReviewWithCommentDto : HotelReviewWithCommentDto,
        val imageUrls : List<String>
)

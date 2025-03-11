package com.ll.hotel.domain.review.review.dto.response;

import com.ll.hotel.standard.page.dto.PageDto;

data class HotelReviewListResponse(
    val hotelReviewPage: PageDto<HotelReviewResponse>,
    val averageRating: Double
)

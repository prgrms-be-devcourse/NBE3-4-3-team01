package com.ll.hotel.domain.review.review.dto.response;

import com.ll.hotel.standard.page.dto.PageDto;

public record HotelReviewListResponse (
        PageDto<HotelReviewResponse> hotelReviewPage,
        Double averageRating
        ){
}

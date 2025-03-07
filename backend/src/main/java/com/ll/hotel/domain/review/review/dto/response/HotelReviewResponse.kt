package com.ll.hotel.domain.review.review.dto.response;

import java.util.List;

public record HotelReviewResponse(
        HotelReviewWithCommentDto hotelReviewWithCommentDto,
        List<String> imageUrls
) {
}

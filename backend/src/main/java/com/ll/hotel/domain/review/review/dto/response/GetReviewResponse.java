package com.ll.hotel.domain.review.review.dto.response;

import com.ll.hotel.domain.review.review.dto.ReviewDto;

import java.util.List;

public record GetReviewResponse(
        ReviewDto reviewDto,
        List<String> imageUrls
) {
}

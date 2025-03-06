package com.ll.hotel.domain.review.review.dto.response;

import java.util.List;

public record MyReviewResponse(
        MyReviewWithCommentDto myReviewWithCommentDto,
        List<String> imageUrls
) {
}

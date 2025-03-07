package com.ll.hotel.domain.review.review.dto.response;

data class MyReviewResponse(
        val myReviewWithCommentDto : MyReviewWithCommentDto,
        val imageUrls : List<String>
)

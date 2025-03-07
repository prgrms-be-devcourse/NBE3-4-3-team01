package com.ll.hotel.domain.review.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

data class ReviewCommentContentRequest(
    @field: NotBlank(message = "리뷰 답변 내용은 필수입니다.")
    val content: String
)

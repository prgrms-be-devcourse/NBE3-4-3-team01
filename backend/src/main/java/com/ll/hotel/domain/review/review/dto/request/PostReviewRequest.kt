package com.ll.hotel.domain.review.review.dto.request;

import jakarta.validation.constraints.*;

data class PostReviewRequest(
    @field: NotBlank(message = "리뷰 내용은 필수입니다")
    @field: Size(min = 5, max = 300, message = "리뷰 내용은 5자 이상, 300자 이하여야 합니다.")
    val content: String,

    @field: NotNull(message = "리뷰 레이팅은 필수입니다")
    @field: Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
    @field: Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
    val rating: Int,

    @field: NotNull(message = "빈 배열이 아닌 Null 값은 불가능합니다")
    val imageExtensions: List<String>
)

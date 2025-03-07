package com.ll.hotel.domain.review.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

data class UpdateReviewRequest(
    @field: NotBlank(message = "리뷰 내용은 필수입니다")
    val content: String,

    @field: NotNull(message = "리뷰 레이팅은 필수입니다")
    @field: Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
    @field: Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
    val rating: Int,

    @field: NotNull(message = "빈 배열이 아닌 Null 값은 불가능합니다")
    val deleteImageUrls: List<String>,

    @field: NotNull(message = "빈 배열이 아닌 Null 값은 불가능합니다")
    val newImageExtensions: List<String>
)

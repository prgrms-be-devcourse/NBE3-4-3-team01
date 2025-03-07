package com.ll.hotel.domain.review.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateReviewRequest(
        @NotBlank(message = "리뷰 내용은 필수입니다")
        String content,

        @NotNull(message = "리뷰 레이팅은 필수입니다")
        @Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
        @Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
        Integer rating,

        @NotNull(message = "빈 배열이 아닌 Null 값은 불가능합니다")
        List<String> deleteImageUrls,

        @NotNull(message = "빈 배열이 아닌 Null 값은 불가능합니다")
        List<String> newImageExtensions
) {
}

package com.ll.hotel.domain.review.review.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public record PostReviewRequest(
        @NotBlank(message = "리뷰 내용은 필수입니다")
        @Size(min = 5, max = 300, message = "리뷰 내용은 5자 이상, 300자 이하여야 합니다.")
        String content,

        @NotNull(message = "리뷰 레이팅은 필수입니다")
        @Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
        @Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
        Integer rating,

        @NotNull(message = "빈 배열이 아닌 Null 값은 불가능합니다")
        List<String> imageExtensions
) {
}

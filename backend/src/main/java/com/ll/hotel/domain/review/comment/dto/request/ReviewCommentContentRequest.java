package com.ll.hotel.domain.review.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReviewCommentContentRequest(
        @NotBlank(message = "리뷰 답변 내용은 필수입니다.")
        String content
) {
}

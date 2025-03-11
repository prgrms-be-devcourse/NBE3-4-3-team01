package com.ll.hotel.domain.review.comment.entity;

import com.ll.hotel.domain.review.review.entity.Review;
import com.ll.hotel.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "review_comments")
class ReviewComment(
    @OneToOne(fetch = FetchType.LAZY)
    @NotNull(message = "리뷰 정보는 필수입니다.")
    val review: Review,

    @Column(length = 1000)
    @Size(min = 5, max = 200, message = "답변 내용은 필수입니다.")
    var content: String
) : BaseTime() {
}
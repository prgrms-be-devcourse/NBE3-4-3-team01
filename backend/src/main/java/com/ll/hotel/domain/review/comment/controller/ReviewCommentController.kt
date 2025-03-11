package com.ll.hotel.domain.review.comment.controller;

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.review.comment.dto.request.ReviewCommentContentRequest
import com.ll.hotel.domain.review.comment.service.ReviewCommentService
import com.ll.hotel.global.request.Rq
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews/{reviewId}/comments")
@Tag(name = "ReviewCommentController")
class ReviewCommentController(
    private val reviewCommentService: ReviewCommentService,
    private val rq: Rq
) {

    @PostMapping("")
    @Operation(summary = "리뷰 답변 생성")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun createReviewComment(
        @PathVariable("reviewId") reviewId: Long,
        @RequestBody @Valid contentRequest: ReviewCommentContentRequest
    ) {
        val actor: Member = rq.getActor();

        reviewCommentService.createReviewComment(actor, reviewId, contentRequest.content);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "리뷰 답변 수정")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateReviewComment(
        @PathVariable("reviewId") reviewId: Long,
        @PathVariable("commentId") commentId: Long,
        @RequestBody @Valid contentRequest: ReviewCommentContentRequest
    ) {
        val actor: Member = rq.getActor();

        reviewCommentService.updateReviewComment(actor, commentId, contentRequest.content);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "리뷰 답변 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReviewComment(
        @PathVariable("reviewId") reviewId: Long,
        @PathVariable("commentId") commentId: Long
    ) {
        val actor: Member = rq.getActor();

        reviewCommentService.deleteReviewComment(actor, commentId);
    }
}

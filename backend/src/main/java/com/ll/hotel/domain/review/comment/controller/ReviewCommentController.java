package com.ll.hotel.domain.review.comment.controller;

import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.review.comment.dto.request.ReviewCommentContentRequest;
import com.ll.hotel.domain.review.comment.service.ReviewCommentService;
import com.ll.hotel.global.request.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews/{reviewId}/comments")
@RequiredArgsConstructor
@Tag(name = "ReviewCommentController")
public class ReviewCommentController {
    private final ReviewCommentService reviewCommentService;
    private final Rq rq;

    @PostMapping("")
    @Operation(summary = "리뷰 답변 생성")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createReviewComment(
            @PathVariable("reviewId") long reviewId,
            @RequestBody @Valid ReviewCommentContentRequest contentRequest
    ) {
        Member actor = rq.getActor();

        reviewCommentService.createReviewComment(actor, reviewId, contentRequest.content());
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "리뷰 답변 수정")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateReviewComment(
            @PathVariable("reviewId") long reviewId,
            @PathVariable("commentId") long commentId,
            @RequestBody @Valid ReviewCommentContentRequest contentRequest
    ) {
        Member actor = rq.getActor();

        reviewCommentService.updateReviewComment(actor, commentId, contentRequest.content());
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "리뷰 답변 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReviewComment(
            @PathVariable("reviewId") long reviewId,
            @PathVariable("commentId") long commentId
    ) {
        Member actor = rq.getActor();

        reviewCommentService.deleteReviewComment(actor, commentId);
    }
}

package com.ll.hotel.domain.review.comment.service;

import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.review.comment.entity.ReviewComment
import com.ll.hotel.domain.review.comment.repository.ReviewCommentRepository
import com.ll.hotel.domain.review.review.entity.Review
import com.ll.hotel.domain.review.review.repository.ReviewRepository
import com.ll.hotel.global.exceptions.ErrorCode
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ReviewCommentService(
    private val reviewCommentRepository: ReviewCommentRepository,
    private val hotelRepository: HotelRepository,
    private val reviewRepository: ReviewRepository,
    private val entityManager: EntityManager
) {
    fun createReviewComment(actor: Member, reviewId: Long, content: String): ReviewComment {
        val review: Review = reviewRepository.findById(reviewId)
            .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::throwServiceException);

        val reviewComment: ReviewComment = ReviewComment(review, content);

        if (!review.hotel.isOwnedBy(actor)) {
            ErrorCode.REVIEW_COMMENT_CREATION_FORBIDDEN.throwServiceException();
        }

        return reviewCommentRepository.save(reviewComment);
    }

    fun updateReviewComment(actor: Member, reviewCommentId: Long, content: String) {
        val reviewComment: ReviewComment = reviewCommentRepository.findById(reviewCommentId)
            .orElseThrow(ErrorCode.REVIEW_COMMENT_NOT_FOUND::throwServiceException);

        if (!reviewComment.review.hotel.isOwnedBy(actor)) {
            ErrorCode.REVIEW_COMMENT_UPDATE_FORBIDDEN.throwServiceException();
        }

        reviewComment.content = content;
    }

    fun deleteReviewComment(actor: Member, reviewCommentId: Long) {
        val reviewComment: ReviewComment = reviewCommentRepository.findById(reviewCommentId)
            .orElseThrow(ErrorCode.REVIEW_COMMENT_NOT_FOUND::throwServiceException);

        if (!reviewComment.review.hotel.isOwnedBy(actor)) {
            ErrorCode.REVIEW_COMMENT_DELETE_FORBIDDEN.throwServiceException();
        }

        reviewComment.review.reviewComment = null;
    }
}

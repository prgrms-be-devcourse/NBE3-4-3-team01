package com.ll.hotel.domain.review.comment.service;

import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.review.comment.entity.ReviewComment;
import com.ll.hotel.domain.review.comment.repository.ReviewCommentRepository;
import com.ll.hotel.domain.review.review.entity.Review;
import com.ll.hotel.domain.review.review.repository.ReviewRepository;
import com.ll.hotel.global.exceptions.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommentService {
    private final ReviewCommentRepository reviewCommentRepository;
    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;
    private final EntityManager entityManager;

    public ReviewComment createReviewComment(Member actor, long reviewId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::throwServiceException);

        ReviewComment reviewComment = new ReviewComment(review, content);

        if(!review.getHotel().isOwnedBy(actor)) {
            ErrorCode.REVIEW_COMMENT_CREATION_FORBIDDEN.throwServiceException();
        }

        return reviewCommentRepository.save(reviewComment);
    }

    public void updateReviewComment(Member actor, long reviewCommentId, String content) {
        ReviewComment reviewComment = reviewCommentRepository.findById(reviewCommentId)
                .orElseThrow(ErrorCode.REVIEW_COMMENT_NOT_FOUND::throwServiceException);

        if(!reviewComment.getReview().getHotel().isOwnedBy(actor)) {
            ErrorCode.REVIEW_COMMENT_UPDATE_FORBIDDEN.throwServiceException();
        }

        reviewComment.setContent(content);
    }

    public void deleteReviewComment(Member actor, long reviewCommentId) {
        ReviewComment reviewComment = reviewCommentRepository.findById(reviewCommentId)
                .orElseThrow(ErrorCode.REVIEW_COMMENT_NOT_FOUND::throwServiceException);

        if(!reviewComment.getReview().getHotel().isOwnedBy(actor)) {
            ErrorCode.REVIEW_COMMENT_DELETE_FORBIDDEN.throwServiceException();
        }

        reviewComment.getReview().setReviewComment(null);
    }
}

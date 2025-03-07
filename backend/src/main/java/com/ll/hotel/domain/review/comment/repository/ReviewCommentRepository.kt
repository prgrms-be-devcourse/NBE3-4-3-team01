package com.ll.hotel.domain.review.comment.repository;

import com.ll.hotel.domain.review.comment.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ReviewCommentRepository : JpaRepository<ReviewComment, Long>

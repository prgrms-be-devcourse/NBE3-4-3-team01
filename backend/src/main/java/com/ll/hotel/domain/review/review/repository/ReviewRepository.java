package com.ll.hotel.domain.review.review.repository;

import com.ll.hotel.domain.review.review.dto.response.HotelReviewWithCommentDto;
import com.ll.hotel.domain.review.review.dto.response.MyReviewWithCommentDto;
import com.ll.hotel.domain.review.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 멤버 ID로 리뷰 목록 조회
    @Query("""  
        SELECT new com.ll.hotel.domain.review.review.dto.response.MyReviewWithCommentDto(
            h.hotelName,
            r.roomName,
            rv,
            rc,
            b.createdAt
        )
        FROM Review rv
        JOIN rv.hotel h
        JOIN rv.room r
        JOIN rv.booking b
        LEFT JOIN ReviewComment rc ON rc.review = rv
        WHERE rv.member.id = :memberId
    """)
    Page<MyReviewWithCommentDto> findReviewsWithCommentByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // 호텔 ID로 리뷰 목록 조회
    @Query("""  
        SELECT new com.ll.hotel.domain.review.review.dto.response.HotelReviewWithCommentDto(
            m.memberEmail,
            r.roomName,
            rv,
            rc,
            b.createdAt
        )
        FROM Review rv
        JOIN rv.hotel h
        JOIN rv.room r
        JOIN rv.booking b
        JOIN rv.member m
        LEFT JOIN ReviewComment rc ON rc.review = rv
        WHERE h.id = :hotelId
    """)
    Page<HotelReviewWithCommentDto> findReviewsWithCommentByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    @Query("""
        SELECT rv
        FROM Review rv
        WHERE rv.member.id = :memberId
        ORDER BY rv.id DESC
    """)
    List<Review> findByMemberId(@Param("memberId") Long memberId);

    @Query("""
        SELECT rv
        FROM Review rv
        WHERE rv.hotel.id = :hotelId
        ORDER BY rv.id DESC
    """)
    List<Review> findByHotelId(@Param("hotelId") Long hotelId);
}


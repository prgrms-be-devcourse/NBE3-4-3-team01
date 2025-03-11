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


@Repository
interface ReviewRepository : JpaRepository<Review, Long> {

    // 멤버 ID로 리뷰 목록 조회
    @Query(
        """  
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
    """
    )
    fun findReviewsWithCommentByMemberId(
        @Param("memberId") memberId: Long,
        pageable: Pageable
    ): Page<MyReviewWithCommentDto>

    // 호텔 ID로 리뷰 목록 조회
    @Query(
        """  
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
    """
    )
    fun findReviewsWithCommentByHotelId(
        @Param("hotelId") hotelId: Long,
        pageable: Pageable
    ): Page<HotelReviewWithCommentDto>

    @Query(
        """
        SELECT rv
        FROM Review rv
        WHERE rv.member.id = :memberId
        ORDER BY rv.id DESC
    """
    )
    fun findByMemberId(@Param("memberId") memberId: Long): List<Review>

    @Query(
        """
        SELECT rv
        FROM Review rv
        WHERE rv.hotel.id = :hotelId
        ORDER BY rv.id DESC
    """
    )
    fun findByHotelId(@Param("hotelId") hotelId: Long): List<Review>
}


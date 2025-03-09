package com.ll.hotel.domain.review.review.entity

import com.ll.hotel.domain.booking.booking.entity.Booking
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.review.comment.entity.ReviewComment
import com.ll.hotel.global.jpa.entity.BaseTime
import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
@Table(name = "reviews")
class Review(
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "호텔 정보는 필수입니다.")
    val hotel: Hotel,

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "객실 정보는 필수입니다.")
    val room: Room,

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "예약 정보는 필수입니다.")
    val booking: Booking,

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "작성자 정보는 필수입니다.")
    val member: Member,

    @Column(length = 300, nullable = false)
    @NotNull(message = "리뷰 내용은 필수입니다.")
    @Size(min = 5, max = 300, message = "리뷰 내용은 5자 이상, 300자 이하여야 합니다.")
    var content: String,

    @Column(nullable = false)
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
    var rating: Int
) : BaseTime() {

    @OneToOne(mappedBy = "review", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    var reviewComment: ReviewComment? = null

    fun isWrittenBy(member: Member): Boolean = this.member.id == member.id
}
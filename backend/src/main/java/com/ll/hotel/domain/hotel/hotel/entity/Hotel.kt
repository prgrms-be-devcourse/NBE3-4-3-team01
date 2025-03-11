package com.ll.hotel.domain.hotel.hotel.entity

import com.ll.hotel.domain.hotel.hotel.dto.PostHotelRequest
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.hotel.option.entity.HotelOption
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.global.jpa.entity.BaseTime
import jakarta.persistence.*
import java.time.LocalTime

@Entity
class Hotel(
    @Column
    var hotelName: String,

    @Column(unique = true)
    var hotelEmail: String,

    @Column
    var hotelPhoneNumber: String,

    @Column
    var streetAddress: String,

    @Column
    var zipCode: Int,

    @Column
    var hotelGrade: Int,

    @Column
    var checkInTime: LocalTime,

    @Column
    var checkOutTime: LocalTime,

    @Column(columnDefinition = "TEXT")
    var hotelExplainContent: String,

    @Column
    @Enumerated(EnumType.STRING)
    var hotelStatus: HotelStatus = HotelStatus.PENDING,

    @OneToMany(mappedBy = "hotel", cascade = [CascadeType.ALL], orphanRemoval = true)
    var rooms: MutableList<Room> = mutableListOf(),

    @OneToOne(fetch = FetchType.LAZY)
    var business: Business,

    @ManyToMany
    var hotelOptions: MutableSet<HotelOption> = mutableSetOf(),

    @ManyToMany
    var favorites: MutableSet<Member> = mutableSetOf(),

    @Column
    var averageRating: Double = 0.0,

    @Column
    var totalReviewRatingSum: Long = 0L,

    @Column
    var totalReviewCount: Long = 0L
) : BaseTime() {
    // 평균 레이팅 업데이트
    fun updateAverageRating(countOffset: Int, ratingOffset: Int) {
        this.totalReviewCount += countOffset
        this.totalReviewRatingSum += ratingOffset
        this.averageRating = Math.round((this.totalReviewRatingSum.toDouble() / this.totalReviewCount) * 10.0) / 10.0
    }

    fun isOwnedBy(member: Member): Boolean {
        return this.business.member == member
    }

    companion object {
        fun hotelBuild(request: PostHotelRequest, business: Business, hotelOptions: MutableSet<HotelOption>): Hotel {
            return Hotel(
                hotelName = request.hotelName,
                hotelEmail = request.hotelEmail,
                hotelPhoneNumber = request.hotelPhoneNumber,
                streetAddress = request.streetAddress,
                zipCode = request.zipCode,
                hotelGrade = request.hotelGrade,
                checkInTime = request.checkInTime,
                checkOutTime = request.checkOutTime,
                hotelExplainContent = request.hotelExplainContent,
                business = business,
                hotelOptions = hotelOptions
            )
        }
    }
}
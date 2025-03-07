package com.ll.hotel.domain.member.admin.dto.response

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import java.time.LocalDate
import java.time.LocalTime

class AdminHotelResponse {
    data class ApprovalResult(
        val name: String,
        val status: HotelStatus
    ) {
        companion object {
            fun from(hotel: Hotel) = ApprovalResult(
                name = hotel.hotelName,
                status = hotel.hotelStatus
            )
        }
    }

    data class Summary(
        val hotelId: Long,
        val name: String,
        val streetAddress: String,
        val ownerName: String,
        val status: HotelStatus
    ) {
        companion object {
            fun from(hotel: Hotel) = Summary(
                hotelId = hotel.id,
                name = hotel.hotelName,
                streetAddress = hotel.streetAddress,
                ownerName = hotel.business.member.memberName,
                status = hotel.hotelStatus
            )
        }
    }

    data class Detail(
        val hotelName: String,
        val streetAddress: String,
        val zipCode: Int,
        val hotelGrade: Int,
        val checkInTime: LocalTime,
        val checkOutTime: LocalTime,
        val hotelStatus: HotelStatus,

        val hotelEmail: String,
        val hotelPhoneNumber: String,

        val ownerId: Long,
        val ownerName: String,
        val businessRegistrationNumber: String,
        val startDate: LocalDate,

        val averageRating: Double,
        val totalReviewCount: Long,

        val hotelOptions: Set<String>?
    ) {
        companion object {
            fun from(hotel: Hotel) = Detail(
                hotelName = hotel.hotelName,
                streetAddress = hotel.streetAddress,
                zipCode = hotel.zipCode,
                hotelGrade = hotel.hotelGrade,
                checkInTime = hotel.checkInTime,
                checkOutTime = hotel.checkOutTime,
                hotelStatus = hotel.hotelStatus,

                hotelEmail = hotel.business.member.memberEmail,
                hotelPhoneNumber = hotel.business.member.memberPhoneNumber,

                ownerId = hotel.business.member.id,
                ownerName = hotel.business.member.memberName,
                businessRegistrationNumber = hotel.business.businessRegistrationNumber,
                startDate = hotel.business.startDate,

                averageRating = hotel.averageRating,
                totalReviewCount = hotel.totalReviewCount,
                hotelOptions = hotel.hotelOptions?.map { it.name }?.toSet() ?: emptySet()
            )
        }
    }
}

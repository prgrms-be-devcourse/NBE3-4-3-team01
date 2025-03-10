package com.ll.hotel.domain.member.admin.dto.response

import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.domain.member.member.type.MemberStatus
import java.time.LocalDate

class AdminBusinessResponse {
    data class ApprovalResult(
        val businessId: Long,
        val businessRegistrationNumber: String,
        val startDate: LocalDate,
        val ownerName: String,
        val approvalStatus: BusinessApprovalStatus,
    ) {
        companion object {
            fun from(business: Business) = ApprovalResult(
                businessId = business.id,
                businessRegistrationNumber = business.businessRegistrationNumber,
                startDate = business.startDate,
                ownerName = business.ownerName,
                approvalStatus = business.approvalStatus
            )
        }
    }

    data class Summary(
        val businessId: Long,
        val ownerName: String,
        val contact: String,
        val approvalStatus: BusinessApprovalStatus,
        val hotelName: String?
    ) {
        companion object {
            fun from(business: Business) = Summary(
                businessId = business.id,
                ownerName = business.ownerName,
                contact = business.member.memberPhoneNumber,
                approvalStatus = business.approvalStatus,
                hotelName = business.hotel?.hotelName
            )
        }
    }

    data class Detail(
        val businessId: Long,
        val businessRegistrationNumber: String,
        val ownerName: String,
        val startDate: LocalDate,

        val memberName: String,
        val memberEmail: String,
        val memberPhoneNumber: String,
        val memberStatus: MemberStatus,

        val hotelId: Long?,
        val hotelName: String?,
        val streetAddress: String?,
        val hotelStatus: HotelStatus?,

        val approvalStatus: BusinessApprovalStatus
    ) {
        companion object {
            fun from(business: Business) = Detail(
                businessId = business.id,
                businessRegistrationNumber = business.businessRegistrationNumber,
                ownerName = business.ownerName,
                startDate = business.startDate,

                memberName = business.member.memberName,
                memberEmail = business.member.memberEmail,
                memberPhoneNumber = business.member.memberPhoneNumber,
                memberStatus = business.member.memberStatus,

                hotelId = business.hotel?.id,
                hotelName = business.hotel?.hotelName,
                streetAddress = business.hotel?.streetAddress,
                hotelStatus = business.hotel?.hotelStatus,

                approvalStatus = business.approvalStatus,
            )
        }
    }
}

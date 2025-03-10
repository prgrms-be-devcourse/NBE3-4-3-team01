package com.ll.hotel.domain.member.member.dto.response

import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import java.time.LocalDate

class BusinessResponse {
    data class ApprovalResult(
        val businessId: Long,
        val businessRegistrationNumber: String,
        val startDate: LocalDate,
        val ownerName: String,
        val approvalStatus: BusinessApprovalStatus
    ) {
        companion object {
            fun of(business: Business): ApprovalResult {
                return ApprovalResult(
                    business.id,
                    business.businessRegistrationNumber,
                    business.startDate,
                    business.ownerName,
                    business.approvalStatus
                )
            }
        }
    }

    data class Verification(
        val data: List<Map<String, Any>>
    )
}
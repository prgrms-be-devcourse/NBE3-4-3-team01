package com.ll.hotel.domain.member.admin.dto.request

import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import jakarta.validation.constraints.NotNull

data class AdminBusinessRequest(
    @field:NotNull
    val businessApprovalStatus: BusinessApprovalStatus
)

package com.ll.hotel.domain.member.member.type

enum class BusinessApprovalStatus(val description: String) {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED("승인 거절")
}
package com.ll.hotel.domain.member.member.type;

public enum BusinessApprovalStatus {
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    REJECTED ("승인 거절"),
    CANCELED("승인 취소");

    private String description;

    BusinessApprovalStatus(String description) {
        this.description = description;
    }
}

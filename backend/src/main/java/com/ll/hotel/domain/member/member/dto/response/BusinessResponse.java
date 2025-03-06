package com.ll.hotel.domain.member.member.dto.response;

import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BusinessResponse {
    public record ApprovalResult(
            Long businessId,
            String businessRegistrationNumber,
            LocalDate startDate,
            String ownerName,
            BusinessApprovalStatus approvalStatus
    ) {
        public static ApprovalResult of(Business business) {
            return new ApprovalResult(
                    business.getId(),
                    business.getBusinessRegistrationNumber(),
                    business.getStartDate(),
                    business.getOwnerName(),
                    business.getApprovalStatus()
            );
        }
    }

    public record Verification(
            List<Map<String, Object>> data
    ) {}
}

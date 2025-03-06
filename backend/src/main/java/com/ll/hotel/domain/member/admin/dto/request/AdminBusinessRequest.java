package com.ll.hotel.domain.member.admin.dto.request;

import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AdminBusinessRequest(
        @NotNull(message = "사업자 승인 상태는 필수 항목입니다.")
        BusinessApprovalStatus businessApprovalStatus
) {}

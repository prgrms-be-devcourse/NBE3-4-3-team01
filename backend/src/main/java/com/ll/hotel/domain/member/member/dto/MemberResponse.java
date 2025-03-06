package com.ll.hotel.domain.member.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberResponse(
        @NotNull(message = "회원 정보는 필수입니다.")
        MemberDTO memberDto,

        @NotBlank(message = "메시지는 필수입니다.")
        String message
) {}

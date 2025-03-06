package com.ll.hotel.domain.member.member.dto;

import com.ll.hotel.domain.member.member.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record JoinRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(
        regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
        message = "전화번호는 010-XXXX-XXXX 형식으로 입력해주세요."
    )
    String phoneNumber,

    @NotNull(message = "회원 유형은 필수입니다.") 
    Role role,

    @NotBlank(message = "OAuth 제공자는 필수입니다.")
    String provider,
    
    @NotBlank(message = "OAuth ID는 필수입니다.")
    String oauthId,

    @NotNull(message = "생년월일은 필수입니다.")
    LocalDate birthDate
) {}
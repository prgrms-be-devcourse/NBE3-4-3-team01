package com.ll.hotel.domain.member.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record MemberRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String memberEmail,

    @NotBlank(message = "이름은 필수입니다")
    String memberName,

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호는 'XXX-XXXX-XXXX' 형식이어야 합니다.")
    // 정규 표현식은 한국 휴대폰 번호만을 고려, 추후 국제 전화번호를 고려할 경우 정규 표현식을 수정 필요
    String memberPhoneNumber,

    @NotBlank(message = "생년월일은 필수입니다")
    LocalDate birthDate
) {}
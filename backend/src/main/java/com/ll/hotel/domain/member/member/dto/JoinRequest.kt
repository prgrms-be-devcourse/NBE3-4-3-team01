package com.ll.hotel.domain.member.member.dto

import com.ll.hotel.domain.member.member.entity.Role
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class JoinRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,

    @field:NotBlank(message = "전화번호는 필수입니다.")
    @field:Pattern(
        regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
        message = "전화번호는 010-XXXX-XXXX 형식으로 입력해주세요."
    )
    val phoneNumber: String,

    @field:NotNull(message = "회원 유형은 필수입니다.") 
    val role: Role,

    @field:NotBlank(message = "OAuth 제공자는 필수입니다.")
    val provider: String,
    
    @field:NotBlank(message = "OAuth ID는 필수입니다.")
    val oauthId: String,

    @field:NotNull(message = "생년월일은 필수입니다.")
    val birthDate: LocalDate
) 
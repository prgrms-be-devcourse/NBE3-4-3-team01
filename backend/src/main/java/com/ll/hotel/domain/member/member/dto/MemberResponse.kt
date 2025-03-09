package com.ll.hotel.domain.member.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class MemberResponse(
    @field:NotNull(message = "회원 정보는 필수입니다.")
    val memberDto: MemberDTO,

    @field:NotBlank(message = "메시지는 필수입니다.")
    val message: String
) 
package com.ll.hotel.domain.member.member.dto

import java.time.LocalDate
import java.time.LocalDateTime

import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.domain.member.member.entity.Member

data class MemberDTO(
    val id: Long,
    val memberEmail: String,
    val memberName: String,
    val memberPhoneNumber: String,
    val birthDate: LocalDate?,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val role: Role,
    val memberStatus: MemberStatus
) {
    companion object {
        @JvmStatic
        fun from(member: Member): MemberDTO {
            return MemberDTO(
                id = member.id,
                memberEmail = member.memberEmail,
                memberName = member.memberName,
                memberPhoneNumber = member.memberPhoneNumber,
                birthDate = member.birthDate,
                createdAt = member.createdAt,
                modifiedAt = member.modifiedAt,
                role = member.role,
                memberStatus = member.memberStatus
            )
        }
    }
} 
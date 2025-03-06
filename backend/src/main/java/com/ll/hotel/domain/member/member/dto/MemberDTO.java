package com.ll.hotel.domain.member.member.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import com.ll.hotel.domain.member.member.entity.Member;

public record MemberDTO(
        Long id,
        String memberEmail,
        String memberName,
        String memberPhoneNumber,
        LocalDate birthDate,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Role role,
        MemberStatus memberStatus
) {
    public static MemberDTO from(Member member) {
        return new MemberDTO(
            member.getId(),
            member.getMemberEmail(),
            member.getMemberName(),
            member.getMemberPhoneNumber(),
            member.getBirthDate(),
            member.getCreatedAt(),
            member.getModifiedAt(),
            member.getRole(),
            member.getMemberStatus()
        );
    }
}
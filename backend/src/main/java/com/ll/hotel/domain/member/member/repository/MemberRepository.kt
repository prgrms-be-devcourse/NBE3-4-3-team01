package com.ll.hotel.domain.member.member.repository

import com.ll.hotel.domain.member.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findByMemberEmail(memberEmail: String): Optional<Member>
    fun findByMemberName(nickname: String): Optional<Member>
    fun existsByMemberEmail(memberEmail: String): Boolean
} 
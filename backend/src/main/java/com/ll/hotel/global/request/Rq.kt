package com.ll.hotel.global.request

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.security.oauth2.dto.SecurityUser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class Rq(private val memberRepository: MemberRepository) {
    private var actor: Member? = null

    fun getActor(): Member {
        if (actor == null) {
            actor = SecurityContextHolder.getContext().authentication
                ?.let { it.principal as? SecurityUser }
                ?.let { securityUser -> memberRepository.findByMemberEmail(securityUser.email) }
                ?.orElseThrow { ErrorCode.UNAUTHORIZED.throwServiceException() }
        }

        return actor!!
    }
}
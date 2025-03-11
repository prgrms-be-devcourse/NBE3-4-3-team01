package com.ll.hotel.domain.member.member.repository

import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BusinessRepository: JpaRepository<Business, Long> {
    fun findByMember(member: Member): Business?

    fun findByApprovalStatus(approvalStatus: BusinessApprovalStatus, pageable: Pageable): Page<Business>
}
package com.ll.hotel.domain.member.admin.service

import com.ll.hotel.domain.member.admin.dto.request.AdminBusinessRequest
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.global.exceptions.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminBusinessService(
    private val businessRepository: BusinessRepository
) {
    fun findAllPaged(page: Int): Page<Business> {
        val pageable = PageRequest.of(page, 10)
        val pagedBusiness = businessRepository.findAll(pageable)

        if (pagedBusiness.hasContent().not()) {
            ErrorCode.PAGE_NOT_FOUND.throwServiceException()
        }

        return pagedBusiness
    }

    fun findById(id: Long): Business = businessRepository.findById(id)
        .orElseThrow { ErrorCode.BUSINESS_NOT_FOUND.throwServiceException() }

    @Transactional
    fun approve(business: Business, adminBusinessRequest: AdminBusinessRequest) {
        business.approvalStatus = adminBusinessRequest.businessApprovalStatus

        val member: Member = business.member
        member.role = Role.BUSINESS
    }

    fun flush() = businessRepository.flush()
}
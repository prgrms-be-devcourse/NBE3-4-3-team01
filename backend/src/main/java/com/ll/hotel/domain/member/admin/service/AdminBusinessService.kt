package com.ll.hotel.domain.member.admin.service

import com.ll.hotel.domain.member.admin.dto.request.AdminBusinessRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminBusinessResponse
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.standard.page.dto.PageDto
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminBusinessService(
    private val businessRepository: BusinessRepository
) {
    fun findAllPaged(page: Int): PageDto<AdminBusinessResponse.Summary> {
        val pageable = PageRequest.of(page, 10)
        val pagedBusiness = businessRepository.findAll(pageable)

        if (pagedBusiness.hasContent().not()) {
            ErrorCode.PAGE_NOT_FOUND.throwServiceException()
        }

        val pagedSummaries = pagedBusiness.map(AdminBusinessResponse.Summary::from)

        return PageDto(pagedSummaries)
    }

    fun findById(id: Long): AdminBusinessResponse.Detail {
        val business = businessRepository.findById(id)
            .orElseThrow(ErrorCode.BUSINESS_NOT_FOUND::throwServiceException)

        return AdminBusinessResponse.Detail.from(business)
    }

    @Transactional
    fun approve(id: Long, adminBusinessRequest: AdminBusinessRequest): AdminBusinessResponse.ApprovalResult {
        val business = businessRepository.findById(id)
            .orElseThrow(ErrorCode.BUSINESS_NOT_FOUND::throwServiceException)

        business.approvalStatus = adminBusinessRequest.businessApprovalStatus
        business.member.role = Role.BUSINESS

        businessRepository.flush()

        return AdminBusinessResponse.ApprovalResult.from(business)
    }
}
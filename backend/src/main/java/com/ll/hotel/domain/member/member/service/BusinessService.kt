package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest
import com.ll.hotel.domain.member.member.dto.response.BusinessResponse
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BusinessService(
    private val businessRepository: BusinessRepository,
    private val businessValidationService: BusinessValidationService
) {
    @Transactional
    fun register(
        registrationInfo: BusinessRequest.RegistrationInfo,
        member: Member
    ): BusinessResponse.ApprovalResult {

        businessValidationService.validateBusiness(registrationInfo)
        member.role = Role.BUSINESS

        val business = Business(
            businessRegistrationNumber = registrationInfo.businessRegistrationNumber,
            startDate = registrationInfo.startDate,
            ownerName = registrationInfo.ownerName,
            approvalStatus = BusinessApprovalStatus.APPROVED,
            member = member
        )

        val savedBusiness = businessRepository.save(business)

        return BusinessResponse.ApprovalResult.of(savedBusiness)
    }
}
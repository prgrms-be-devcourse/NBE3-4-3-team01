package com.ll.hotel.domain.member.member.controller

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest
import com.ll.hotel.domain.member.member.dto.response.BusinessResponse
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.service.BusinessService
import com.ll.hotel.domain.member.member.service.BusinessValidationService
import com.ll.hotel.global.request.Rq
import com.ll.hotel.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "BusinessController", description = "Business API")
@RestController
@RequestMapping("/api/businesses")
class BusinessController(
    private val businessService: BusinessService,
    private val businessValidationService: BusinessValidationService,
    private val rq: Rq
) {

    @Operation(summary = "사업자 등록")
    @PostMapping("/register")
    fun register(
        @RequestBody @Valid registrationInfo: BusinessRequest.RegistrationInfo
    ): RsData<BusinessResponse.ApprovalResult> {

        val member: Member = rq.actor

        val validationResult: String = businessValidationService.validateBusiness(registrationInfo)

        val business: Business = businessService.register(registrationInfo, member, validationResult)

        return RsData.success(HttpStatus.CREATED, BusinessResponse.ApprovalResult.of(business))
    }
}
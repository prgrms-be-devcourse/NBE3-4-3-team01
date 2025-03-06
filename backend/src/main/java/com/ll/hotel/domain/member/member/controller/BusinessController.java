package com.ll.hotel.domain.member.member.controller;

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest;
import com.ll.hotel.domain.member.member.dto.response.BusinessResponse;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.service.BusinessService;
import com.ll.hotel.domain.member.member.service.BusinessValidationService;
import com.ll.hotel.global.request.Rq;
import com.ll.hotel.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@Tag(name = "BusinessController")
public class BusinessController {
    private final BusinessService businessService;
    private final BusinessValidationService businessValidationService;
    private final Rq rq;

    @Operation(summary = "사업자 등록")
    @PostMapping("/register")
    public RsData<BusinessResponse.ApprovalResult> register(@RequestBody @Valid BusinessRequest.RegistrationInfo registrationInfo) {

        Member member =rq.getActor();

        String validationResult = businessValidationService.validateBusiness(registrationInfo);

        Business business = businessService.register(registrationInfo, member, validationResult);

        return RsData.success(HttpStatus.CREATED, BusinessResponse.ApprovalResult.of(business));
    }
}

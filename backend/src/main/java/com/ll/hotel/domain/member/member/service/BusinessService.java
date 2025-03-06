package com.ll.hotel.domain.member.member.service;

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.repository.BusinessRepository;
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;
import com.ll.hotel.global.exceptions.ErrorCode;
import com.ll.hotel.global.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository businessRepository;

    @Transactional
    public Business register(BusinessRequest.RegistrationInfo registrationInfo, Member member, String validationResult) {

        if (validationResult.equals("01")) {
            member.setRole(Role.BUSINESS);
        } else {
            ErrorCode.INVALID_BUSINESS_INFO.throwServiceException();
        }

        Business business = Business
                .builder()
                .businessRegistrationNumber(registrationInfo.businessRegistrationNumber())
                .startDate(registrationInfo.startDate())
                .ownerName(registrationInfo.ownerName())
                .approvalStatus(BusinessApprovalStatus.APPROVED)
                .member(member)
                .hotel(null)
                .build();
        return businessRepository.save(business);
    }
}

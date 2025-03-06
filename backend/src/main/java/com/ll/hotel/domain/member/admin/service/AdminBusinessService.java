package com.ll.hotel.domain.member.admin.service;

import com.ll.hotel.domain.member.admin.dto.request.AdminBusinessRequest;
import com.ll.hotel.domain.member.admin.dto.response.AdminBusinessResponse;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.repository.BusinessRepository;
import com.ll.hotel.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBusinessService {
    private final BusinessRepository businessRepository;

    public Page<Business> findAllPaged(int page) {
        Pageable pageable = PageRequest.of(page, 10);

        Page<Business> pagedBusiness = businessRepository.findAll(pageable);

        if (!pagedBusiness.hasContent()) {
            ErrorCode.PAGE_NOT_FOUND.throwServiceException();
        }

        return pagedBusiness;
    }

    public Business findById(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(ErrorCode.BUSINESS_NOT_FOUND::throwServiceException);
    }

    @Transactional
    public void approve(Business business, AdminBusinessRequest adminBusinessRequest) {
        business.setApprovalStatus(adminBusinessRequest.businessApprovalStatus());

        Member member = business.getMember();
        member.setRole(Role.BUSINESS);
    }

    public void flush() {
        businessRepository.flush();
    }
}

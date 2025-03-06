package com.ll.hotel.domain.member.admin.controller;

import com.ll.hotel.domain.member.admin.dto.request.AdminBusinessRequest;
import com.ll.hotel.domain.member.admin.dto.response.AdminBusinessResponse;
import com.ll.hotel.domain.member.admin.service.AdminBusinessService;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.global.response.RsData;
import com.ll.hotel.standard.page.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/businesses")
@RequiredArgsConstructor
@Tag(name = "AdminBusinessController")
public class AdminBusinessController {
    private final AdminBusinessService adminBusinessService;

    @Operation(summary = "사업자 전체 조회")
    @GetMapping
    public RsData<PageDto<AdminBusinessResponse.Summary>> getAll(
            @RequestParam(value = "page", defaultValue = "0") int page) {

        Page<AdminBusinessResponse.Summary> pagedBusinessSummaries = adminBusinessService.findAllPaged(page)
                .map(AdminBusinessResponse.Summary::from);

        return RsData.success(HttpStatus.OK, new PageDto<>(pagedBusinessSummaries));
    }

    @Operation(summary = "사업자 단건 조회")
    @GetMapping("/{id}")
    public RsData<AdminBusinessResponse.Detail> getById(@PathVariable("id") Long id) {

        Business business = adminBusinessService.findById(id);

        return RsData.success(HttpStatus.OK, AdminBusinessResponse.Detail.from(business));
    }

    @Operation(summary = "사업자 승인")
    @PatchMapping("/{id}")
    public RsData<AdminBusinessResponse.ApprovalResult> approve(@PathVariable("id") Long id,
                                                 @RequestBody @Valid AdminBusinessRequest adminBusinessRequest) {
        Business business = adminBusinessService.findById(id);

        adminBusinessService.approve(business, adminBusinessRequest);

        adminBusinessService.flush();

        return RsData.success(HttpStatus.OK, AdminBusinessResponse.ApprovalResult.from(business));
    }
}

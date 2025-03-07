package com.ll.hotel.domain.member.admin.controller

import com.ll.hotel.domain.member.admin.dto.request.AdminBusinessRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminBusinessResponse
import com.ll.hotel.domain.member.admin.service.AdminBusinessService
import com.ll.hotel.global.response.RsData
import com.ll.hotel.standard.page.dto.PageDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "AdminBusinessController", description = "Admin Business API")
@RestController
@RequestMapping("/api/admin/businesses")
class AdminBusinessController(
    private val adminBusinessService: AdminBusinessService
) {
    @Operation(summary = "사업자 전체 조회")
    @GetMapping
    fun getAll(@RequestParam(value = "page", defaultValue = "0") page: Int)
    : RsData<PageDto<AdminBusinessResponse.Summary>> {
        val pagedBusinessSummaries: Page<AdminBusinessResponse.Summary> =
            adminBusinessService.findAllPaged(page).map(AdminBusinessResponse.Summary::from)

        return RsData.success(HttpStatus.OK, PageDto(pagedBusinessSummaries))
    }

    @Operation(summary = "사업자 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long) : RsData<AdminBusinessResponse.Detail> {
        val business = adminBusinessService.findById(id)

        return RsData.success(HttpStatus.OK, AdminBusinessResponse.Detail.from(business))
    }

    @Operation(summary = "사업자 승인")
    @PatchMapping("/{id}")
    fun approve(@PathVariable("id") id: Long,
                @RequestBody @Valid adminBusinessRequest: AdminBusinessRequest)
    : RsData<AdminBusinessResponse.ApprovalResult> {
        val business = adminBusinessService.findById(id)
        adminBusinessService.approve(business, adminBusinessRequest)
        adminBusinessService.flush()
        return RsData.success(HttpStatus.OK, AdminBusinessResponse.ApprovalResult.from(business))
    }
}
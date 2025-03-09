package com.ll.hotel.domain.member.admin.controller

import com.ll.hotel.domain.member.admin.dto.request.AdminBusinessRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminBusinessResponse
import com.ll.hotel.domain.member.admin.service.AdminBusinessService
import com.ll.hotel.global.response.RsData
import com.ll.hotel.standard.page.dto.PageDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
    fun getAll(@RequestParam(value = "page", defaultValue = "0") page: Int
    ): RsData<PageDto<AdminBusinessResponse.Summary>> =
        RsData.success(HttpStatus.OK, adminBusinessService.findAllPaged(page))

    @Operation(summary = "사업자 단건 조회")
    @GetMapping("/{id}")
    fun getById(
        @PathVariable("id") id: Long
    ) : RsData<AdminBusinessResponse.Detail> =
        RsData.success(HttpStatus.OK, adminBusinessService.findById(id))

    @Operation(summary = "사업자 승인")
    @PatchMapping("/{id}")
    fun approve(@PathVariable("id") id: Long,
                @RequestBody @Valid adminBusinessRequest: AdminBusinessRequest
    ): RsData<AdminBusinessResponse.ApprovalResult> =
        RsData.success(HttpStatus.OK, adminBusinessService.approve(id, adminBusinessRequest))
}
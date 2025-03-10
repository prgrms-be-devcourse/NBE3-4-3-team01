package com.ll.hotel.domain.member.admin.controller

import com.ll.hotel.domain.member.admin.dto.request.AdminHotelRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminHotelResponse
import com.ll.hotel.domain.member.admin.service.AdminHotelService
import com.ll.hotel.global.response.RsData
import com.ll.hotel.standard.page.dto.PageDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "AdminHotelController", description = "Admin Hotel API")
@RestController
@RequestMapping("/api/admin/hotels")
class AdminHotelController(
    private val adminHotelService: AdminHotelService
) {
    @Operation(summary = "호텔 전체 조회")
    @GetMapping
    fun getAll(@RequestParam(value = "page", defaultValue = "0") page: Int)
    : RsData<PageDto<AdminHotelResponse.Summary>> =
        RsData.success(HttpStatus.OK, adminHotelService.findAllPaged(page))

    @Operation(summary = "호텔 단건 조회")
    @GetMapping("/{id}")
    fun getById(
        @PathVariable("id") id: Long
    ) : RsData<AdminHotelResponse.Detail> =
        RsData.success(HttpStatus.OK, adminHotelService.findById(id))

    @Operation(summary = "호텔 승인")
    @PatchMapping("/{id}")
    fun approve(@PathVariable("id") id: Long,
                @RequestBody @Valid adminHotelRequest: AdminHotelRequest
    ): RsData<AdminHotelResponse.ApprovalResult> =
        RsData.success(HttpStatus.OK, adminHotelService.approve(id, adminHotelRequest))
}
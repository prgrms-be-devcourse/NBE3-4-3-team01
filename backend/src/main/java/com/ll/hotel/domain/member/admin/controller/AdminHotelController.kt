package com.ll.hotel.domain.member.admin.controller

import com.ll.hotel.domain.member.admin.dto.request.AdminHotelRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminHotelResponse
import com.ll.hotel.domain.member.admin.service.AdminHotelService
import com.ll.hotel.global.response.RsData
import com.ll.hotel.standard.page.dto.PageDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
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
    : RsData<PageDto<AdminHotelResponse.Summary>> {
        val pagedHotelSummaries: Page<AdminHotelResponse.Summary> =
            adminHotelService.findAllPaged(page).map(AdminHotelResponse.Summary::from);
        return RsData.success(HttpStatus.OK, PageDto(pagedHotelSummaries));
    }

    @Operation(summary = "호텔 단건 조회")
    @GetMapping("/{id}")
    fun getById(@PathVariable("id") id: Long) : RsData<AdminHotelResponse.Detail> {
        val hotel = adminHotelService.findById(id);

        return RsData.success(HttpStatus.OK, AdminHotelResponse.Detail.from(hotel));
    }

    @Operation(summary = "호텔 승인")
    @PatchMapping("/{id}")
    fun approve(@PathVariable("id") id: Long,
                @RequestBody @Valid adminHotelRequest: AdminHotelRequest)
    : RsData<AdminHotelResponse.ApprovalResult> {
        val hotel = adminHotelService.findById(id);
        adminHotelService.approve(hotel, adminHotelRequest);
        adminHotelService.flush();
        return RsData.success(HttpStatus.OK, AdminHotelResponse.ApprovalResult.from(hotel));
    }
}
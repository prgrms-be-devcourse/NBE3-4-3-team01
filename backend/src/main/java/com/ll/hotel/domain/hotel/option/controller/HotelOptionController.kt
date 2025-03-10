package com.ll.hotel.domain.hotel.option.controller

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest
import com.ll.hotel.domain.hotel.option.dto.response.OptionResponse
import com.ll.hotel.domain.hotel.option.service.HotelOptionService
import com.ll.hotel.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "HotelOptionController", description = "HotelOption API")
@RestController
@RequestMapping("/api/admin/hotel-options")
class HotelOptionController(
    private val hotelOptionService: HotelOptionService
) {
    @Operation(summary = "호텔 옵션 추가")
    @PostMapping
    fun add(@RequestBody @Valid optionRequest: OptionRequest): RsData<OptionResponse> =
        RsData.success(HttpStatus.CREATED, hotelOptionService.add(optionRequest))

    @Operation(summary = "호텔 옵션 전체 조회")
    @GetMapping
    fun getAll(): RsData<List<OptionResponse>> =
        RsData.success(HttpStatus.OK, hotelOptionService.findAll())

    @Operation(summary = "호텔 옵션 수정")
    @PatchMapping("/{id}")
    fun modify(@PathVariable id: Long,
               @RequestBody @Valid optionRequest: OptionRequest
    ): RsData<OptionResponse> =
        RsData.success(HttpStatus.OK, hotelOptionService.modify(id, optionRequest))

    @Operation(summary = "호텔 옵션 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        hotelOptionService.delete(id)
    }
}
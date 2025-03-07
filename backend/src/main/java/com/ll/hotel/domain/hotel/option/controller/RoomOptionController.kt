package com.ll.hotel.domain.hotel.option.controller

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest
import com.ll.hotel.domain.hotel.option.dto.response.OptionResponse
import com.ll.hotel.domain.hotel.option.service.RoomOptionService
import com.ll.hotel.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "RoomOptionController", description = "RoomOption API")
@RestController
@RequestMapping("/api/admin/room-options")
class RoomOptionController(
    private val roomOptionService: RoomOptionService
) {
    @Operation(summary = "객실 옵션 추가")
    @PostMapping
    fun add(@RequestBody @Valid optionRequest: OptionRequest): RsData<OptionResponse> =
        RsData.success(HttpStatus.CREATED, roomOptionService.add(optionRequest))

    @Operation(summary = "객실 옵션 전체 조회")
    @GetMapping
    fun getAll(): RsData<List<OptionResponse>> =
        RsData.success(HttpStatus.OK, roomOptionService.findAll())

    @Operation(summary = "객실 옵션 수정")
    @PatchMapping("/{id}")
    fun modify(@PathVariable id: Long,
               @RequestBody @Valid optionRequest: OptionRequest
    ): RsData<OptionResponse> =
        RsData.success(HttpStatus.OK, roomOptionService.modify(id, optionRequest))

    @Operation(summary = "객실 옵션 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        roomOptionService.delete(id)
    }
}
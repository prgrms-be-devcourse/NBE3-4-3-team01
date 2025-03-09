package com.ll.hotel.domain.hotel.hotel.controller

import com.ll.hotel.domain.hotel.hotel.dto.*
import com.ll.hotel.domain.hotel.hotel.service.HotelService
import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.global.request.Rq
import com.ll.hotel.global.response.RsData
import com.ll.hotel.global.validation.GlobalValidation.checkCheckInAndOutDate
import com.ll.hotel.global.validation.GlobalValidation.checkPageSize
import com.ll.hotel.standard.page.dto.PageDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels")
@Tag(name = "HotelController")
class HotelController(
    private val hotelService: HotelService,
    private val rq: Rq
) {
    @PostMapping
    @Operation(summary = "호텔 등록")
    fun create(@RequestBody postHotelRequest: @Valid PostHotelRequest): RsData<PostHotelResponse> {
        val actor: Member = this.rq.getActor()

        return RsData.success(
            HttpStatus.CREATED, this.hotelService.createHotel(actor, postHotelRequest)
        )
    }

    @PostMapping("/{hotelId}/urls")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "사진 URL 리스트 저장")
    fun saveImageUrls(
        @PathVariable hotelId: Long,
        @RequestBody urls: List<String>,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val actor: Member = this.rq.getActor()

        this.hotelService.saveImages(actor, ImageType.HOTEL, hotelId, urls)
        this.hotelService.updateRoleCookie(request, response, hotelId)
    }

    @GetMapping
    @Operation(
        summary = "조건에 맞는 전체 호텔 목록",
        description = """
                    검색 조건에 맞는 호텔 목록을 보여줍니다.<br>
                    해당 날짜 및 인원으로 예약 가능한 객실이 존재하지 않으면, 목록에 노출하지 않습니다.
                    """
    )
    fun findAllHotels(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "latest") filterName: String,
        @RequestParam(required = false) filterDirection: String?,
        @RequestParam(defaultValue = "") streetAddress: String,
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkInDate: LocalDate,
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().plusDays(1)}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkoutDate: LocalDate,
        @RequestParam(defaultValue = "2") personal: Int
    ): RsData<PageDto<GetHotelResponse>> {
        checkPageSize(pageSize)
        checkCheckInAndOutDate(checkInDate!!, checkoutDate!!)

        return RsData.success(
            HttpStatus.OK,
            PageDto(
                this.hotelService.findAllHotels(
                    page, pageSize, filterName, filterDirection, streetAddress,
                    checkInDate, checkoutDate, personal
                )
            )
        )
    }

    @GetMapping("/{hotelId}")
    @Operation(
        summary = "호텔 상세 정보",
        description = """
                    호텔의 상세 정보 및 객실 목록을 보여줍니다.<br>
                    요청받은 인원을 수용할 수 있는 객실만 보여줍니다.<br>
                    잔여 객실이 없다면 예약 버튼을 클릭할 수 없습니다. 다만, 목록에는 노출됩니다.
                    """
    )
    fun findHotelDetailWithAvailableRooms(
        @PathVariable hotelId: Long,
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkInDate: LocalDate,
        @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().plusDays(1)}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkoutDate: LocalDate,
        @RequestParam(defaultValue = "2") personal: Int
    ): RsData<GetHotelDetailResponse> {
        return RsData.success(
            HttpStatus.OK,
            this.hotelService.findHotelDetailWithAvailableRooms(hotelId, checkInDate, checkoutDate, personal)
        )
    }

    @GetMapping("/{hotelId}/business")
    @Operation(
        summary = "사업자 전용 호텔 상세 정보",
        description = """
                    사업자 전용 호텔 상세 정보를 보여줍니다.<br>
                    호텔이 보유한 모든 객실 목록을 노출합니다.
                    """
    )
    fun findHotelDetail(@PathVariable hotelId: Long): RsData<GetHotelDetailResponse> {
        return RsData.success(HttpStatus.OK, this.hotelService.findHotelDetail(hotelId))
    }

    @PutMapping("/{hotelId}")
    @Operation(summary = "호텔 수정")
    fun modifyHotel(
        @PathVariable hotelId: Long,
        @RequestBody request: @Valid PutHotelRequest
    ): RsData<PutHotelResponse> {
        val actor: Member = this.rq.getActor()

        return RsData.success(
            HttpStatus.OK,
            this.hotelService.modifyHotel(hotelId, actor, request)
        )
    }

    @DeleteMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "호텔 삭제",
        description = """
                    호텔을 삭제합니다.<br>
                    정확하게는 호텔을 사용불가 상태로 변경합니다.
                    """
    )
    fun deleteHotel(@PathVariable hotelId: Long) {
        val actor: Member = this.rq.getActor()

        this.hotelService.deleteHotel(hotelId, actor)
    }

    @GetMapping("{hotelId}/revenue")
    @Operation(summary = "호텔 매출 정보")
    fun findHotelRevenue(@PathVariable hotelId: Long): RsData<GetHotelRevenueResponse> {
        val actor: Member = this.rq.getActor()

        return RsData.success(
            HttpStatus.OK,
            this.hotelService.findRevenue(hotelId, actor)
        )
    }

    @GetMapping("/hotel-option")
    @Operation(
        summary = "호텔 옵션 정보",
        description = """
                    호텔에 등록할 수 있는 모든 호텔 옵션 정보를 불러옵니다.<br>
                    사업자는 소유 호텔에서 제공하는 옵션을 체크하여 등록 및 수정할 수 있습니다.<br>
                    등록되지 않은 호텔 옵션이 존재할 시, 관리자에 요청해야합니다.
                    """
    )
    fun findAllHotelOptions(): RsData<GetAllHotelOptionsResponse> {
        val actor: Member = this.rq.getActor()

        return RsData.success(HttpStatus.OK, this.hotelService.findHotelOptions(actor))
    }
}
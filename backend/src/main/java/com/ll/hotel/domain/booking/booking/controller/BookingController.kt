package com.ll.hotel.domain.booking.booking.controller

import com.ll.hotel.domain.booking.booking.dto.*
import com.ll.hotel.domain.booking.booking.service.BookingService
import com.ll.hotel.domain.member.member.dto.MemberDTO
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.global.mail.MailService
import com.ll.hotel.global.request.Rq
import com.ll.hotel.global.response.RsData
import com.ll.hotel.standard.page.dto.PageDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "BookingController", description = "예약 관련 API")
class BookingController(
    private val bookingService: BookingService,
    private val mailService: MailService,
    private val rq: Rq
) {

    @GetMapping
    @Operation(summary = "예약 페이지 정보 요청", description = "예약에 필요한 정보들을 요청하는 api")
    fun preBook(
        @RequestParam(name = "hotelId") hotelId: Long,
        @RequestParam(name = "roomId") roomId: Long): RsData<BookingFormResponse> {
        val actor: Member = rq.getActor()

        return RsData.success(
            HttpStatus.OK,
            bookingService.preCreate(hotelId, roomId, actor)
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "예약 및 결제", description = "예약 및 결제 정보를 저장하는 api")
    fun book(
        @RequestBody @Valid bookingRequest: BookingRequest) {
        val actor: Member = rq.getActor()
        val booking: BookingResponseDetails = bookingService.create(actor, bookingRequest)

        // 예약 확정 메일링
        mailService.sendBookingConfirmedMail(MemberDTO.from(actor), booking);
    }

    @GetMapping("/me")
    @Operation(summary = "내 예약 조회", description = "사용자의 예약 내역을 조회하는 api")
    fun getMyBookings(
        @RequestParam(defaultValue = "1", name = "page") page: Int,
        @RequestParam(defaultValue = "5", name = "page_size") pageSize: Int): RsData<PageDto<BookingResponseSummary>> {
        val actor: Member = rq.getActor()

        return RsData.success(
            HttpStatus.OK,
            PageDto(bookingService.tryGetMyBookings(actor, page, pageSize))
        )
    }

    @GetMapping("/myHotel")
    @Operation(summary = "호텔측 예약 조회", description = "호텔의 예약 내역을 조회하는 api")
    fun getHotelBookings(
        @RequestParam(defaultValue = "1", name = "page") page: Int,
        @RequestParam(defaultValue = "5", name = "page_size") pageSize: Int): RsData<PageDto<BookingResponseSummary>> {
        val actor: Member = rq.getActor()

        return RsData.success(
            HttpStatus.OK,
            PageDto(bookingService.tryGetHotelBookings(actor, page, pageSize))
        )
    }

    @GetMapping("/{booking_id}")
    @Operation(summary = "예약 상세 조회", description = "예약의 상세 정보를 조회하는 api")
    fun getBookingDetails(
        @PathVariable("booking_id") bookingId: Long): RsData<BookingResponseDetails> {
        val actor: Member = rq.getActor()

        return RsData.success(
            HttpStatus.OK,
            bookingService.tryGetBookingDetails(actor, bookingId)
        )
    }

    @DeleteMapping("/{booking_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "예약 취소", description = "예약을 취소하는 api")
    fun cancel(
        @PathVariable("booking_id") bookingId: Long) {
        val actor: Member = rq.getActor()
        val booking: BookingResponseDetails = bookingService.tryCancel(actor, bookingId)

        // 예약 취소 메일링
        mailService.sendBookingCancelledMail(MemberDTO.from(actor), booking)
    }

    @PatchMapping("/{booking_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "예약 완료 처리", description = "예약을 완료 처리하는 api")
    fun complete(
        @PathVariable("booking_id") bookingId: Long) {
        val actor: Member = rq.getActor()
        bookingService.trySetCompleted(actor, bookingId)
    }
}
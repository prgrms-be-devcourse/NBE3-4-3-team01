package com.ll.hotel.domain.booking.booking.controller;

import com.ll.hotel.domain.booking.booking.dto.*;
import com.ll.hotel.domain.booking.booking.service.BookingService;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.request.Rq;
import com.ll.hotel.global.response.RsData;
import com.ll.hotel.standard.page.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "BookingController", description = "예약 관련 API")
public class BookingController {
    private final BookingService bookingService;
    private final Rq rq;

    @GetMapping
    @Operation(summary = "예약 페이지 정보 요청", description = "예약에 필요한 정보들을 요청하는 api")
    public RsData<BookingFormResponse> preBook(
            @RequestParam(name = "hotelId") long hotelId,
            @RequestParam(name = "roomId") long roomId) {
        Member actor = rq.getActor();

        return RsData.success(
                HttpStatus.OK,
                bookingService.preCreate(hotelId, roomId, actor)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "예약 및 결제", description = "예약 및 결제 정보를 저장하는 api")
    public void book(
            @RequestBody @Valid BookingRequest bookingRequest) {
        Member actor = rq.getActor();
        bookingService.create(actor, bookingRequest);
    }

    @GetMapping("/me")
    @Operation(summary = "내 예약 조회", description = "사용자의 예약 내역을 조회하는 api")
    public RsData<PageDto<BookingResponseSummary>> getMyBookings(
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "5", name = "page_size") int pageSize) {
        Member actor = rq.getActor();

        return RsData.success(
                HttpStatus.OK,
                new PageDto<>(bookingService.tryGetMyBookings(actor, page, pageSize))
        );
    }

    @GetMapping("/myHotel")
    @Operation(summary = "호텔측 예약 조회", description = "호텔의 예약 내역을 조회하는 api")
    public RsData<PageDto<BookingResponseSummary>> getHotelBookings(
            @RequestParam(defaultValue = "1", name = "page") int page,
            @RequestParam(defaultValue = "5", name = "page_size") int pageSize) {
        Member actor = rq.getActor();

        return RsData.success(
                HttpStatus.OK,
                new PageDto<>(bookingService.tryGetHotelBookings(actor, page, pageSize))
        );
    }

    @GetMapping("/{booking_id}")
    @Operation(summary = "예약 상세 조회", description = "예약의 상세 정보를 조회하는 api")
    public RsData<BookingResponseDetails> getBookingDetails(
            @PathVariable("booking_id") long bookingId) {
        Member actor = rq.getActor();

        return RsData.success(
                HttpStatus.OK,
                bookingService.tryGetBookingDetails(actor, bookingId)
        );
    }

    @DeleteMapping("/{booking_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "예약 취소", description = "예약을 취소하는 api")
    public void cancel(
            @PathVariable("booking_id") long bookingId) {
        Member actor = rq.getActor();
        bookingService.tryCancel(actor, bookingId);
    }

    @PatchMapping("/{booking_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "예약 완료 처리", description = "예약을 완료 처리하는 api")
    public void complete(
            @PathVariable("booking_id") long bookingId) {
        Member actor = rq.getActor();
        bookingService.trySetCompleted(actor, bookingId);
    }
}
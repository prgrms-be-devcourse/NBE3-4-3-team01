package com.ll.hotel.domain.booking.booking.dto;

import com.ll.hotel.domain.booking.booking.type.BookingStatus;
import com.ll.hotel.domain.booking.payment.dto.PaymentResponse;
import com.ll.hotel.domain.hotel.hotel.dto.HotelDetailDto;
import com.ll.hotel.domain.hotel.room.dto.RoomDto;
import com.ll.hotel.domain.member.member.dto.MemberDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponseDetails(
        long bookingId,
        HotelDetailDto hotel,
        RoomDto room,
        String[] thumbnailUrls,
        MemberDTO member,
        PaymentResponse payment,
        String bookNumber,
        BookingStatus bookingStatus,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}

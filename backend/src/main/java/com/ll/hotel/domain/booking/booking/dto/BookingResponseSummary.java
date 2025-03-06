package com.ll.hotel.domain.booking.booking.dto;

import com.ll.hotel.domain.booking.booking.type.BookingStatus;
import java.time.LocalDate;

public record BookingResponseSummary(
        long bookingId,
        long hotelId,
        long roomId,
        String hotelName,
        String roomName,
        String memberName,
        String thumbnailUrl,
        BookingStatus bookingStatus,
        int amount,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
}

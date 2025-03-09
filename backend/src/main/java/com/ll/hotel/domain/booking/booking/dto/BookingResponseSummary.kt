package com.ll.hotel.domain.booking.booking.dto

import com.ll.hotel.domain.booking.booking.type.BookingStatus
import java.time.LocalDate

data class BookingResponseSummary(
    val bookingId: Long,
    val hotelId: Long,
    val roomId: Long,
    val hotelName: String,
    val roomName: String,
    val memberName: String,
    val thumbnailUrl: String,
    val bookingStatus: BookingStatus,
    val amount: Int,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate
)
package com.ll.hotel.domain.booking.booking.dto

import com.ll.hotel.domain.booking.booking.type.BookingStatus
import com.ll.hotel.domain.booking.payment.dto.PaymentResponse
import com.ll.hotel.domain.hotel.hotel.dto.HotelDetailDto
import com.ll.hotel.domain.hotel.room.dto.RoomDto
import com.ll.hotel.domain.member.member.dto.MemberDTO
import java.time.LocalDate
import java.time.LocalDateTime

data class BookingResponseDetails(
    val bookingId: Long,
    val hotel: HotelDetailDto,
    val room: RoomDto,
    val thumbnailUrls: List<String>,
    val member: MemberDTO,
    val payment: PaymentResponse,
    val bookNumber: String,
    val bookingStatus: BookingStatus,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate
)
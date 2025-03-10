package com.ll.hotel.domain.booking.booking.dto

import com.ll.hotel.domain.booking.booking.entity.Booking
import com.ll.hotel.domain.booking.booking.type.BookingStatus
import com.ll.hotel.domain.booking.payment.dto.PaymentResponse
import com.ll.hotel.domain.member.member.dto.MemberDTO
import java.time.LocalDate
import java.time.LocalDateTime

data class BookingResponse(
    val bookingId: Long,
    val roomId: Long,
    val hotelId: Long,
    val member: MemberDTO,
    val payment: PaymentResponse,
    val bookNumber: String,
    val bookingStatus: BookingStatus,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate
) {
    companion object {
        fun from(booking: Booking): BookingResponse {
            return BookingResponse(
                bookingId = booking.id,
                roomId = booking.room.id,
                hotelId = booking.hotel.id,
                member = MemberDTO.from(booking.member),
                payment = PaymentResponse.from(booking.payment),
                bookNumber = booking.bookingNumber,
                bookingStatus = booking.bookingStatus,
                createdAt = booking.createdAt,
                modifiedAt = booking.modifiedAt,
                checkInDate = booking.checkInDate,
                checkOutDate = booking.checkOutDate
            )
        }
    }
}
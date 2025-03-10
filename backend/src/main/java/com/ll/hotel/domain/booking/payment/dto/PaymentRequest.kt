package com.ll.hotel.domain.booking.payment.dto

import com.ll.hotel.domain.booking.booking.dto.BookingRequest
import jakarta.validation.constraints.NotNull

data class PaymentRequest(
    @field:NotNull(message = "거래 UID는 필수입니다.")
    val merchantUid: String,

    @field:NotNull(message = "거래 금액은 필수입니다.")
    val amount: Int,

    @field:NotNull(message = "거래 일자는 필수입니다.")
    val paidAtTimestamp: Long
) {
    companion object {
        @JvmStatic
        fun from(bookingRequest: BookingRequest): PaymentRequest {
            return PaymentRequest(
                merchantUid = bookingRequest.merchantUid,
                amount = bookingRequest.amount,
                paidAtTimestamp = bookingRequest.paidAtTimestamp
            )
        }
    }
}
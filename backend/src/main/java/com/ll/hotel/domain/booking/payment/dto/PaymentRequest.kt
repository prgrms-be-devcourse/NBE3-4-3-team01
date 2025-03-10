package com.ll.hotel.domain.booking.payment.dto

import com.ll.hotel.domain.booking.booking.dto.BookingRequest
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class PaymentRequest(
    @field:NotNull(message = "거래 UID는 필수입니다.")
    @field:Size(min = 10, max = 10, message = "거래 Uid는 10자리여야 합니다.")
    val merchantUid: String,

    @field:NotNull(message = "거래 금액은 필수입니다.")
    @field:Min(value = 0, message = "거래 금액은 0 이상이어야 합니다.")
    val amount: Int,

    @field:NotNull(message = "거래 일자는 필수입니다.")
    @field:Min(value = 1, message = "유효한 거래 일자를 입력해주세요.")
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
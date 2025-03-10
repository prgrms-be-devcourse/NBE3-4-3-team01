package com.ll.hotel.domain.booking.payment.dto

import com.ll.hotel.domain.booking.payment.entity.Payment
import com.ll.hotel.domain.booking.payment.type.PaymentStatus
import java.time.LocalDateTime

data class PaymentResponse(
    val paymentId: Long,
    val merchantUid: String,
    val amount: Int,
    val paymentStatus: PaymentStatus,
    val paidAt: LocalDateTime,
    val createdAt: LocalDateTime,
    val modifiedAt: LocalDateTime
) {
    companion object {
        @JvmStatic
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                paymentId = payment.id,
                merchantUid = payment.merchantUid,
                amount = payment.amount,
                paymentStatus = payment.paymentStatus,
                paidAt = payment.paidAt,
                createdAt = payment.createdAt,
                modifiedAt = payment.modifiedAt
            )
        }
    }
}
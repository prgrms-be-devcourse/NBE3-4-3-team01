package com.ll.hotel.domain.booking.payment.entity

import com.ll.hotel.domain.booking.payment.type.PaymentStatus
import com.ll.hotel.global.jpa.entity.BaseTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

@Entity
class Payment(
    @Column(unique = true)
    var merchantUid: String,

    @Column
    var amount: Int,

    @Enumerated(EnumType.STRING)
    @Column
    var paymentStatus: PaymentStatus = PaymentStatus.PAID,

    @Column
    var paidAt: LocalDateTime
) : BaseTime() {

    constructor(
        merchantUid: String,
        amount: Int,
        paidAt: LocalDateTime
    ) : this(
        merchantUid = merchantUid,
        amount = amount,
        paymentStatus = PaymentStatus.PAID,
        paidAt = paidAt
    )
}
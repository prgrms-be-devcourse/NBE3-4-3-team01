package com.ll.hotel.domain.booking.payment.repository

import com.ll.hotel.domain.booking.payment.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun existsByMerchantUid(merchantUid: String): Boolean
}
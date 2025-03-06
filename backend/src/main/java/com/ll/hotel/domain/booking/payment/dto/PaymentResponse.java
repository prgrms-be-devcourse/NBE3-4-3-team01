package com.ll.hotel.domain.booking.payment.dto;

import com.ll.hotel.domain.booking.payment.entity.Payment;
import com.ll.hotel.domain.booking.payment.type.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        long paymentId,
        String merchantUid,
        int amount,
        PaymentStatus paymentStatus,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getMerchantUid(),
                payment.getAmount(),
                payment.getPaymentStatus(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                payment.getModifiedAt()
        );
    }
}

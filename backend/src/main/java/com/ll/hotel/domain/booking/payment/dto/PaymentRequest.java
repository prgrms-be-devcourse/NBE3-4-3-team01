package com.ll.hotel.domain.booking.payment.dto;

import com.ll.hotel.domain.booking.booking.dto.BookingRequest;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "거래 UID는 필수입니다.") String merchantUid,
        @NotNull(message = "거래 금액은 필수입니다.") int amount,
        @NotNull(message = "거래 일자는 필수입니다.") long paidAtTimestamp
) {
    public static PaymentRequest from(BookingRequest bookingRequest) {
        return new PaymentRequest(
                bookingRequest.merchantUid(),
                bookingRequest.amount(),
                bookingRequest.paidAtTimestamp()
        );
    }
}


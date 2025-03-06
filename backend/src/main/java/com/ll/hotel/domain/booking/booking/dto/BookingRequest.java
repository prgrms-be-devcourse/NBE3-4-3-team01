package com.ll.hotel.domain.booking.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "객실 정보는 필수입니다.") long roomId,
        @NotNull(message = "호텔 정보는 필수입니다.") long hotelId,
        @NotNull(message = "체크인 일자는 필수입니다.") LocalDate checkInDate,
        @NotNull(message = "체크아웃 일자는 필수입니다.") LocalDate checkOutDate,

        // PaymentRequest 생성에 필요
        @NotNull(message = "거래 Uid는 필수입니다.") String merchantUid,
        @NotNull(message = "거래 금액은 필수입니다.") int amount,
        @NotNull(message = "거래 일자는 필수입니다.") long paidAtTimestamp
) {
}

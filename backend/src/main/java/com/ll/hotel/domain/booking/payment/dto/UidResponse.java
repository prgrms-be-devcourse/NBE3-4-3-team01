package com.ll.hotel.domain.booking.payment.dto;

public record UidResponse(
        String apiId,
        String channelKey,
        String merchantUid
) {
}

package com.ll.hotel.domain.booking.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) // 불필요한 필드 무시
public record TokenResponse(
        int code,
        String message,
        ResponseData response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResponseData(
            @JsonProperty("access_token") String accessToken,
            long now,
            @JsonProperty("expired_at") long expiredAt
    ) {}
}
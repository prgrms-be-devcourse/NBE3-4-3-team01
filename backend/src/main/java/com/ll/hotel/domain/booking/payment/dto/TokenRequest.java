package com.ll.hotel.domain.booking.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRequest(
        @JsonProperty("imp_key") String impKey,
        @JsonProperty("imp_secret") String impSecret
) {
}

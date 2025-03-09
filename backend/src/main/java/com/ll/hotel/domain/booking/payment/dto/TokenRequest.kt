package com.ll.hotel.domain.booking.payment.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenRequest(
    @JsonProperty("imp_key") val impKey: String,
    @JsonProperty("imp_secret") val impSecret: String
)
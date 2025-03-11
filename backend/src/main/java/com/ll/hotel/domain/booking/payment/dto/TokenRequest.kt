package com.ll.hotel.domain.booking.payment.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class TokenRequest(
    @JsonProperty("imp_key")
    @field:NotNull(message = "Portone API Key를 찾을 수 없습니다.")
    val impKey: String,

    @JsonProperty("imp_secret")
    @field:NotNull(message = "Portone API Key를 찾을 수 없습니다.")
    val impSecret: String
)
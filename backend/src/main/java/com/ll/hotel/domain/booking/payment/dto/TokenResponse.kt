package com.ll.hotel.domain.booking.payment.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true) // 불필요한 필드 무시
data class TokenResponse(
    val code: Int,
    val message: String,
    val response: ResponseData
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ResponseData(
        @JsonProperty("access_token") val accessToken: String,
        val now: Long,
        @JsonProperty("expired_at") val expiredAt: Long
    )
}
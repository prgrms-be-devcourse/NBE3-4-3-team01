package com.ll.hotel.domain.booking.payment.dto

data class UidResponse(
    val apiId: String,
    val channelKey: String,
    val merchantUid: String
)
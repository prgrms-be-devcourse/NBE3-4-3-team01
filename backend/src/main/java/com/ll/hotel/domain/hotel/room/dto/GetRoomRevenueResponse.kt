package com.ll.hotel.domain.hotel.room.dto

import jakarta.validation.constraints.NotBlank

data class GetRoomRevenueResponse (
    val roomId: Long,

    @field:NotBlank
    val roomName: String,

    val basePrice: Int,

    val roomRevenue: Long
)
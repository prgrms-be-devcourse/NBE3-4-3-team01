package com.ll.hotel.domain.hotel.room.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class PostRoomRequest(
    @field:NotBlank
    @field:Length(min = 2, max = 30)
    val roomName: String,

    @field:Min(value = 0)
    val roomNumber: Int,

    val basePrice: Int,

    @field:Min(value = 1)
    val standardNumber: Int,

    @field:Min(value = 1)
    val maxNumber: Int,

    val bedTypeNumber: Map<String, Int> = emptyMap(),

    val imageExtensions: List<String> = emptyList(),

    val roomOptions: Set<String> = emptySet()
)
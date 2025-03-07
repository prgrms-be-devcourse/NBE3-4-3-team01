package com.ll.hotel.domain.member.admin.dto.request

import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import jakarta.validation.constraints.NotNull

data class AdminHotelRequest(
    @field:NotNull
    val hotelStatus: HotelStatus
)

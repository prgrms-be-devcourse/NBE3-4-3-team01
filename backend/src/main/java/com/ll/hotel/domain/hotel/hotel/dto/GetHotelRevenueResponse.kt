package com.ll.hotel.domain.hotel.hotel.dto

import com.ll.hotel.domain.hotel.room.dto.GetRoomRevenueResponse

data class GetHotelRevenueResponse(
    val roomRevenueResponse: List<GetRoomRevenueResponse>,
    val revenue: Long
)
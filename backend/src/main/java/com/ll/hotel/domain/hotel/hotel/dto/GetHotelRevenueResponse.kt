package com.ll.hotel.domain.hotel.hotel.dto

data class GetHotelRevenueResponse(
    val roomRevenueResponse: List<GetHotelRevenueResponse>,
    val revenue: Long
)
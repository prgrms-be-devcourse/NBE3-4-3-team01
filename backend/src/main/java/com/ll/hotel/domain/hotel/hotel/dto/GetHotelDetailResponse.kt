package com.ll.hotel.domain.hotel.hotel.dto

data class GetHotelDetailResponse(
    val hotelDetailDto: HotelDetailDto,
    val hotelImageUrls: List<String> = emptyList()
)
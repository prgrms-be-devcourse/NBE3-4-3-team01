package com.ll.hotel.domain.hotel.hotel.dto

import com.ll.hotel.standard.util.Ut

data class GetHotelDetailResponse(
    val hotelDetailDto: HotelDetailDto,
    val hotelImageUrls: List<String> = emptyList()
)
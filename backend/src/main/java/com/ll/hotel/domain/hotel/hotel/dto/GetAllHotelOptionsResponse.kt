package com.ll.hotel.domain.hotel.hotel.dto

import com.ll.hotel.domain.hotel.option.entity.HotelOption

data class GetAllHotelOptionsResponse(
    val hotelOptions: Set<String>
) {
    constructor(hotelOptions: List<HotelOption>) : this(
        hotelOptions.map { it.name }.toSet()
    )
}
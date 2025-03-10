package com.ll.hotel.domain.hotel.hotel.dto

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.image.entity.Image

data class HotelWithImageDto(
    val hotel: Hotel,
    val image: Image?
)
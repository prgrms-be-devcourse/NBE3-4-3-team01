package com.ll.hotel.domain.hotel.room.dto

import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.image.entity.Image

data class RoomWithImageDto(
    val room: Room,
    val image: Image?
)
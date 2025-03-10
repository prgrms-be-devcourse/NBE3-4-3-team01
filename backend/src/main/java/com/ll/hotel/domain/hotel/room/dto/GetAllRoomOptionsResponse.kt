package com.ll.hotel.domain.hotel.room.dto

import com.ll.hotel.domain.hotel.option.entity.RoomOption

data class GetAllRoomOptionsResponse(
    val roomOptions: Set<String>
) {
    constructor(roomOptions: List<RoomOption>) : this(
        roomOptions.map { it.name }.toSet()
    )
}
package com.ll.hotel.domain.hotel.room.dto

import com.ll.hotel.domain.hotel.room.type.BedTypeNumber
import jakarta.validation.constraints.NotBlank

data class GetRoomResponse(
    val roomId: Long,

    @field:NotBlank
    val roomName: String,

    val basePrice: Int,

    val standardNumber: Int,

    val maxNumber: Int,

    @field:NotBlank
    val bedTypeNumber: BedTypeNumber,

    val thumbnailUrl: String,

    val roomNumber: Int
) {
    constructor(roomWithImageDto: RoomWithImageDto) : this(
        roomWithImageDto.room.id,
        roomWithImageDto.room.roomName,
        roomWithImageDto.room.basePrice,
        roomWithImageDto.room.standardNumber,
        roomWithImageDto.room.maxNumber,
        roomWithImageDto.room.bedTypeNumber,
        roomWithImageDto.image?.imageUrl ?: "/images/default.jpg",
        roomWithImageDto.room.roomNumber
    )
}
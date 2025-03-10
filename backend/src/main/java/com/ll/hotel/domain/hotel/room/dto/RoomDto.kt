package com.ll.hotel.domain.hotel.room.dto

import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.hotel.room.type.BedTypeNumber
import jakarta.validation.constraints.NotBlank

data class RoomDto(
    val id: Long,

    val hotelId: Long,

    @field:NotBlank
    val roomName: String,

    val roomNumber: Int,

    val basePrice: Int,

    val standardNumber: Int,

    val maxNumber: Int,

    @field:NotBlank
    val bedTypeNumber: BedTypeNumber,

    @field:NotBlank
    val roomStatus: String,

    val roomOptions: Set<String>
) {
    constructor(room: Room) : this(
        room.id,
        room.hotel.id,
        room.roomName,
        room.roomNumber,
        room.basePrice,
        room.standardNumber,
        room.maxNumber,
        room.bedTypeNumber,
        room.roomStatus.name,
        room.roomOptions?.map { it.name }?.toSet() ?: emptySet()
    )
}
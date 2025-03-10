package com.ll.hotel.domain.hotel.hotel.dto

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.room.dto.GetRoomResponse
import com.ll.hotel.domain.hotel.room.dto.RoomWithImageDto
import jakarta.validation.constraints.NotBlank
import java.time.LocalTime

data class HotelDetailDto(
    val hotelId: Long,

    @field:NotBlank
    val hotelName: String,

    @field:NotBlank
    val hotelEmail: String,

    @field:NotBlank
    val hotelPhoneNumber: String,

    @field:NotBlank
    val streetAddress: String,

    val zipCode: Int,

    val hotelGrade: Int,

    val checkInTime: LocalTime,

    val checkOutTime: LocalTime,

    @field:NotBlank
    val hotelExplainContent: String,

    @field:NotBlank
    val hotelStatus: String,

    val rooms: List<GetRoomResponse>,

    val hotelOptions: Set<String>
) {
    constructor(hotel: Hotel, dtos: List<RoomWithImageDto>) : this(
        hotel.id,
        hotel.hotelName,
        hotel.hotelEmail,
        hotel.hotelPhoneNumber,
        hotel.streetAddress,
        hotel.zipCode,
        hotel.hotelGrade,
        hotel.checkInTime,
        hotel.checkOutTime,
        hotel.hotelExplainContent,
        hotel.hotelStatus.name,
        dtos.map { GetRoomResponse(it) },
        hotel.hotelOptions?.takeIf { it.isNotEmpty() }?.map { it.name }?.toSet() ?: emptySet()
    )
}
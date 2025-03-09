package com.ll.hotel.domain.hotel.room.dto

import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class PostRoomResponse (
    val roomId: Long,

    val hotelId: Long,

    @field:NotBlank
    val roomName: String,

    val basePrice: Int,

    val standardNumber: Int,

    val maxNumber: Int,

    val createdAt: LocalDateTime,

    val urlsResponse: PresignedUrlsResponse?
){
    constructor(room: Room, response: PresignedUrlsResponse?) : this(
        room.id,
        room.hotel.id,
        room.roomName,
        room.basePrice,
        room.standardNumber,
        room.maxNumber,
        room.createdAt,
        response
    )
}
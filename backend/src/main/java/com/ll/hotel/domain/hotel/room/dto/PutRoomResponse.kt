package com.ll.hotel.domain.hotel.room.dto

import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class PutRoomResponse(
    val hotelId: Long,

    val roomId: Long,

    @field:NotBlank
    val roomName: String,

    @field:NotBlank
    val roomStatus: String,

    val modifiedAt: LocalDateTime,

    val urlResponse: PresignedUrlsResponse?
) {
    constructor(room: Room, response: PresignedUrlsResponse?) : this(
        room.hotel.id,
        room.id,
        room.roomName,
        room.roomStatus.value,
        room.modifiedAt,
        response
    )
}
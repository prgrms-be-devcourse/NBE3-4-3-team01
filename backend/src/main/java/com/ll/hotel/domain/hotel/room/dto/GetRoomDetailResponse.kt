package com.ll.hotel.domain.hotel.room.dto

data class GetRoomDetailResponse(
    val roomDto: RoomDto,
    val roomImageUrls: List<String> = emptyList()
)
package com.ll.hotel.domain.booking.booking.dto

import com.ll.hotel.domain.hotel.hotel.dto.HotelDetailDto
import com.ll.hotel.domain.hotel.room.dto.RoomDto
import com.ll.hotel.domain.member.member.dto.MemberDTO

data class BookingFormResponse(
    val hotel: HotelDetailDto,
    val room: RoomDto,
    val thumbnailUrls: List<String>,
    val member: MemberDTO
)
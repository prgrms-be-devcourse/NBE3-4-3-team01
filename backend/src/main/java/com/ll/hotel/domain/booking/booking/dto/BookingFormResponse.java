package com.ll.hotel.domain.booking.booking.dto;

import com.ll.hotel.domain.hotel.hotel.dto.HotelDetailDto;
import com.ll.hotel.domain.hotel.room.dto.RoomDto;
import com.ll.hotel.domain.member.member.dto.MemberDTO;

public record BookingFormResponse (
        HotelDetailDto hotel,
        RoomDto room,
        String[] thumbnailUrls,
        MemberDTO member
) {
}

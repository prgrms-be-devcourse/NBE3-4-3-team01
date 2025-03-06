package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.standard.util.Ut;
import java.util.List;

public record GetRoomDetailResponse(
        RoomDto roomDto,

        List<String> roomImageUrls
) {
    public GetRoomDetailResponse(RoomDto roomDto, List<String> roomImageUrls) {
        this.roomDto = roomDto;
        this.roomImageUrls = Ut.list.hasValue(roomImageUrls)
                ? roomImageUrls
                : List.of();
    }
}

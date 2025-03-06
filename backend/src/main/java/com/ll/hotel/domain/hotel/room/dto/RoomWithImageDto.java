package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.image.entity.Image;

public record RoomWithImageDto(
        Room room,
        Image image
) {
}

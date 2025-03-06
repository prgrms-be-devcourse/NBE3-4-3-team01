package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import com.ll.hotel.domain.hotel.room.entity.Room;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record GetRoomOptionResponse(
        long roomId,

        Set<String> roomOptions
) {
    public GetRoomOptionResponse(Room room) {
        this(
                room.getId(),
                room.getRoomOptions() != null
                        ? room.getRoomOptions().stream()
                        .map(RoomOption::getName)
                        .collect(Collectors.toSet())
                        : new HashSet<>()
        );
    }
}

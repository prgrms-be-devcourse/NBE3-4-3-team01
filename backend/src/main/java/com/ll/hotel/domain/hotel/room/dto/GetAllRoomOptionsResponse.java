package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record GetAllRoomOptionsResponse(
        Set<String> roomOptions
) {
    public GetAllRoomOptionsResponse(List<RoomOption> roomOptions) {
        this(
                roomOptions.stream()
                        .map(RoomOption::getName)
                        .collect(Collectors.toSet())
        );
    }
}
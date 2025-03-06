package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.hotel.room.type.BedTypeNumber;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

public record RoomDto(
        long id,

        long hotelId,

        @NotBlank
        String roomName,

        @NonNull
        Integer roomNumber,

        @NonNull
        Integer basePrice,

        @NonNull
        Integer standardNumber,

        @NonNull
        Integer maxNumber,

        @NotBlank
        BedTypeNumber bedTypeNumber,

        @NotBlank
        String roomStatus,

        @NonNull
        Set<String> roomOptions
) {
    public RoomDto(Room room) {
        this(
                room.getId(),
                room.getHotel().getId(),
                room.getRoomName(),
                room.getRoomNumber(),
                room.getBasePrice(),
                room.getStandardNumber(),
                room.getMaxNumber(),
                room.getBedTypeNumber(),
                room.getRoomStatus().name(),
                room.getRoomOptions() != null
                        ? room.getRoomOptions().stream()
                        .map(RoomOption::getName)
                        .collect(Collectors.toSet())
                        : new HashSet<>()
        );
    }
}

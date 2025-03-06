package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.domain.hotel.room.type.BedTypeNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record GetRoomResponse(
        long roomId,

        @NotBlank
        String roomName,

        @NonNull
        Integer basePrice,

        @NonNull
        Integer standardNumber,

        @NonNull
        Integer maxNumber,

        @NotBlank
        BedTypeNumber bedTypeNumber,

        @NonNull
        String thumbnailUrl,

        @NonNull
        Integer roomNumber
) {
    public GetRoomResponse(RoomWithImageDto roomWithImageDto) {
        this(
                roomWithImageDto.room().getId(),
                roomWithImageDto.room().getRoomName(),
                roomWithImageDto.room().getBasePrice(),
                roomWithImageDto.room().getStandardNumber(),
                roomWithImageDto.room().getMaxNumber(),
                roomWithImageDto.room().getBedTypeNumber(),
                roomWithImageDto.image() == null
                        ? "/images/default.jpg"
                        : roomWithImageDto.image().getImageUrl(),
                roomWithImageDto.room().getRoomNumber()
        );
    }
}

package com.ll.hotel.domain.hotel.room.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
public record GetRoomRevenueResponse(
        long roomId,

        @NotBlank
        String roomName,

        @NonNull
        Integer basePrice,

        @NonNull
        Long roomRevenue
) {
}

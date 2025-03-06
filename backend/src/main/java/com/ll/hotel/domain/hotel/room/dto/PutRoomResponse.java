package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.NonNull;

public record PutRoomResponse(
        long hotelId,

        long roomId,

        @NotBlank
        String roomName,

        @NotBlank
        String roomStatus,

        @NonNull
        LocalDateTime modifiedAt,

        PresignedUrlsResponse urlResponse
) {
    public PutRoomResponse(Room room, PresignedUrlsResponse response) {
        this(
                room.getHotel().getId(),
                room.getId(),
                room.getRoomName(),
                room.getRoomStatus().getValue(),
                room.getModifiedAt(),
                response
        );
    }
}

package com.ll.hotel.domain.hotel.room.dto;

import com.ll.hotel.domain.hotel.room.type.BedTypeNumber;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.hibernate.validator.constraints.Length;

public record PutRoomRequest(

        @NotBlank @Length(min = 2, max = 30) String roomName,

        @NotNull @Min(value = 0) Integer roomNumber,

        @NotNull Integer basePrice,

        @NotNull @Min(value = 1) Integer standardNumber,

        @NotNull @Min(value = 1) Integer maxNumber,

        @NotNull BedTypeNumber bedTypeNumber,

        @NotBlank String roomStatus,

        List<String> deleteImageUrls,

        List<String> imageExtensions,

        Set<String> roomOptions
) {
    public PutRoomRequest {
        deleteImageUrls = Objects.requireNonNullElse(deleteImageUrls, new ArrayList<>());
        imageExtensions = Objects.requireNonNullElse(imageExtensions, new ArrayList<>());
        roomOptions = Objects.requireNonNullElse(roomOptions, new HashSet<>());
    }
}

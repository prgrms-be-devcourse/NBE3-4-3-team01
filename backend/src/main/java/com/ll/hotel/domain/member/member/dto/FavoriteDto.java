package com.ll.hotel.domain.member.member.dto;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;

import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record FavoriteDto(
        long hotelId,

        @NotBlank
        String hotelName,

        @NotBlank
        String streetAddress,

        @NonNull
        Integer hotelGrade,

        @NotBlank
        String hotelStatus
) {
    public static FavoriteDto from(Hotel hotel) {
        return new FavoriteDto(
                hotel.getId(),
                hotel.getHotelName(),
                hotel.getStreetAddress(),
                hotel.getHotelGrade(),
                hotel.getHotelStatus().getValue()
        );
    }
} 
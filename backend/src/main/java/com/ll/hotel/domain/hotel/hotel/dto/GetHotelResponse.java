package com.ll.hotel.domain.hotel.hotel.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalTime;

public record GetHotelResponse(
        long hotelId,

        @NotBlank
        String hotelName,

        long hotelGrade,

        @NotBlank
        LocalTime checkInTime,

        @NotBlank
        String streetAddress,

        double averageRating,

        long totalReviewCount,

        Integer price,

        @NotBlank
        String thumbnailUrl
) {
    public GetHotelResponse(HotelWithImageDto hotelWithImageDto, Integer price) {
        this(
                hotelWithImageDto.hotel().getId(),
                hotelWithImageDto.hotel().getHotelName(),
                hotelWithImageDto.hotel().getHotelGrade(),
                hotelWithImageDto.hotel().getCheckInTime(),
                hotelWithImageDto.hotel().getStreetAddress(),
                hotelWithImageDto.hotel().getAverageRating(),
                hotelWithImageDto.hotel().getTotalReviewCount(),
                price,
                hotelWithImageDto.image() == null
                        ? "/images/default.jpg"
                        : hotelWithImageDto.image().getImageUrl()
        );
    }
}

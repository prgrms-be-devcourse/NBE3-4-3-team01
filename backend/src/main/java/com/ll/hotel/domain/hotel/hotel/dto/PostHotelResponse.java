package com.ll.hotel.domain.hotel.hotel.dto;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.NonNull;

public record PostHotelResponse(
        long businessId,

        long hotelId,

        @NotBlank
        String hotelName,

        @NonNull
        LocalDateTime createdAt,

        PresignedUrlsResponse urlsResponse
) {
    public PostHotelResponse(Hotel hotel, PresignedUrlsResponse response) {
        this(
                hotel.getBusiness().getId(),
                hotel.getId(),
                hotel.getHotelName(),
                hotel.getCreatedAt(),
                response
        );
    }
}

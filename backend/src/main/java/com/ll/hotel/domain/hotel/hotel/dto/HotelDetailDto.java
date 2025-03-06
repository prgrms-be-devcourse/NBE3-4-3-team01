package com.ll.hotel.domain.hotel.hotel.dto;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.room.dto.GetRoomResponse;
import com.ll.hotel.domain.hotel.room.dto.RoomWithImageDto;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;

public record HotelDetailDto(
        long hotelId,

        @NotBlank
        String hotelName,

        @NotBlank
        String hotelEmail,

        @NotBlank
        String hotelPhoneNumber,

        @NotBlank
        String streetAddress,

        @NonNull
        Integer zipCode,

        @NonNull
        Integer hotelGrade,

        @NonNull
        LocalTime checkInTime,

        @NonNull
        LocalTime checkOutTime,

        @NotBlank
        String hotelExplainContent,

        @NotBlank
        String hotelStatus,

        List<GetRoomResponse> rooms,

        Set<String> hotelOptions
) {
    public HotelDetailDto(Hotel hotel, List<RoomWithImageDto> dtos) {
        this(
                hotel.getId(),
                hotel.getHotelName(),
                hotel.getHotelEmail(),
                hotel.getHotelPhoneNumber(),
                hotel.getStreetAddress(),
                hotel.getZipCode(),
                hotel.getHotelGrade(),
                hotel.getCheckInTime(),
                hotel.getCheckOutTime(),
                hotel.getHotelExplainContent(),
                hotel.getHotelStatus().name(),
                dtos.stream()
                        .map(GetRoomResponse::new)
                        .toList(),
                hotel.getHotelOptions() != null
                ? hotel.getHotelOptions().stream()
                        .map(HotelOption::getName)
                        .collect(Collectors.toSet())
                        : new HashSet<>()
        );
    }
}

package com.ll.hotel.domain.hotel.hotel.dto;

import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record GetAllHotelOptionsResponse(
        Set<String> hotelOptions
) {
    public GetAllHotelOptionsResponse(List<HotelOption> hotelOptions) {
        this(
                hotelOptions.stream()
                        .map(HotelOption::getName)
                        .collect(Collectors.toSet())
        );
    }
}
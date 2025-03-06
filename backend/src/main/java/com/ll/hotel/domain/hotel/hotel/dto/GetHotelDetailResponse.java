package com.ll.hotel.domain.hotel.hotel.dto;

import com.ll.hotel.standard.util.Ut;
import java.util.List;

public record GetHotelDetailResponse(
        HotelDetailDto hotelDetailDto,

        List<String> hotelImageUrls
) {
    public GetHotelDetailResponse(HotelDetailDto hotelDetailDto, List<String> hotelImageUrls) {
        this.hotelDetailDto = hotelDetailDto;
        this.hotelImageUrls = Ut.list.hasValue(hotelImageUrls)
                ? hotelImageUrls
                : List.of();
    }
}

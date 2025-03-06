package com.ll.hotel.domain.hotel.hotel.dto;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.image.entity.Image;

public record HotelWithImageDto(
        Hotel hotel,
        Image image
) {
}

package com.ll.hotel.domain.hotel.hotel.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalTime

data class GetHotelResponse(
    val hotelId: Long,

    @field:NotBlank
    val hotelName: String,

    val hotelGrade: Int,

    @field:NotBlank
    val checkInTime: LocalTime,

    @field:NotBlank
    val streetAddress: String,

    val averageRating: Double,

    val totalReviewCount: Long,

    val price: Int,

    @field:NotBlank
    val thumbnailUrl: String
) {
    constructor(hotelWithImageDto: HotelWithImageDto, price: Int) : this(
        hotelWithImageDto.hotel.id,
        hotelWithImageDto.hotel.hotelName,
        hotelWithImageDto.hotel.hotelGrade,
        hotelWithImageDto.hotel.checkInTime,
        hotelWithImageDto.hotel.streetAddress,
        hotelWithImageDto.hotel.averageRating,
        hotelWithImageDto.hotel.totalReviewCount,
        price,
        hotelWithImageDto.image?.imageUrl ?: "/images/default.jpg"
    )
}
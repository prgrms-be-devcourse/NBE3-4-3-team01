package com.ll.hotel.domain.hotel.hotel.dto

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class PutHotelResponse(
    val businessId: Long,

    val hotelId: Long,

    @field:NotBlank
    val hotelName: String,

    @field:NotBlank
    val hotelStatus: String,

    val modifiedAt: LocalDateTime,

    val urlsResponse: PresignedUrlsResponse?
) {
    constructor(hotel: Hotel, response: PresignedUrlsResponse?) : this(
        hotel.business.id,
        hotel.id,
        hotel.hotelName,
        hotel.hotelStatus.value,
        hotel.modifiedAt,
        response
    )
}
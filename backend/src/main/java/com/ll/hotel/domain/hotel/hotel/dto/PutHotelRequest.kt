package com.ll.hotel.domain.hotel.hotel.dto

import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.Length
import java.time.LocalTime

data class PutHotelRequest(
    @field:NotBlank
    @field:Length(min = 2, max = 30)
    val hotelName: String,

    @field:NotBlank
    @field:Email
    val hotelEmail: String,

    @field:NotBlank
    @field:Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$")
    val hotelPhoneNumber: String,

    @field:NotBlank
    val streetAddress: String,

    val zipCode: Int,

    @field:Min(1)
    @field:Max(5)
    val hotelGrade: Int,

    val checkInTime: LocalTime,

    val checkOutTime: LocalTime,

    @field:NotBlank
    val hotelExplainContent: String,

    @field:NotBlank
    val hotelStatus: String,

    val deleteImageUrls: List<String> = emptyList(),

    val imageExtensions: List<String> = emptyList(),

    val hotelOptions: Set<String> = emptySet()
)
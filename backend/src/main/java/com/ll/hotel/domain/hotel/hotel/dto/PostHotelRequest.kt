package com.ll.hotel.domain.hotel.hotel.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.Length
import java.time.LocalTime

data class PostHotelRequest(
    @field:NotBlank
    @field:Length(min = 2, max = 30)
    val hotelName: String,

    @field:Email
    @field:NotBlank
    val hotelEmail: String,

    @field:NotBlank
    @field:Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$")
    val hotelPhoneNumber: String,

    @field:NotBlank
    val streetAddress: String,

    val zipCode: Int,

    @field:Min(0)
    @field:Max(5)
    val hotelGrade: Int,

    @field:JsonFormat(pattern = "HH:mm")
    val checkInTime: LocalTime,

    @field:JsonFormat(pattern = "HH:mm")
    val checkOutTime: LocalTime,

    @field:NotBlank
    val hotelExplainContent: String,

    val imageExtensions: List<String> = emptyList(),

    val hotelOptions: Set<String> = emptySet()
)
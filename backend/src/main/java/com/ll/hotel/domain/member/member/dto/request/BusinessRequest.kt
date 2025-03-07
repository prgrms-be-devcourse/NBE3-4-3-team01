package com.ll.hotel.domain.member.member.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BusinessRequest {
    data class RegistrationInfo(
        @field:NotBlank
        @field:Pattern(regexp = "^[0-9]{10}$")
        val businessRegistrationNumber: String,

        @field:NotNull
        @field:PastOrPresent
        val startDate: LocalDate,

        @field:NotBlank
        @field:Size(max = 30)
        val ownerName: String
    )

    data class RegistrationApiForm(
        val businesses: List<Map<String, String>>
    ) {
        companion object {
            fun from(registrationInfo: RegistrationInfo): RegistrationApiForm {
                return RegistrationApiForm(
                    listOf(
                        mapOf(
                            "b_no" to registrationInfo.businessRegistrationNumber,
                            "start_dt" to registrationInfo.startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                            "p_nm" to registrationInfo.ownerName
                        )
                    )
                )
            }
        }
    }
}
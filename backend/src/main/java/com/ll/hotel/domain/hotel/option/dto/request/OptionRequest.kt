package com.ll.hotel.domain.hotel.option.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OptionRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val name: String
)

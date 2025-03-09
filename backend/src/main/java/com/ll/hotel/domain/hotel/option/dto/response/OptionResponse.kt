package com.ll.hotel.domain.hotel.option.dto.response

import com.ll.hotel.domain.hotel.option.entity.BaseOption

data class OptionResponse(
    val name: String,
    val optionId: Long
) {
    companion object {
        fun from(option: BaseOption) = OptionResponse(
                optionId = option.id,
                name = option.name
        )
    }
}
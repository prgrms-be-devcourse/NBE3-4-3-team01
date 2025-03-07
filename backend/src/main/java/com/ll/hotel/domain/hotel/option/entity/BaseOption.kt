package com.ll.hotel.domain.hotel.option.entity

import com.ll.hotel.global.jpa.entity.BaseEntity
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@MappedSuperclass
abstract class BaseOption(
    @field:NotBlank
    @field:Size(max = 255)
    var name: String
): BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseOption) return false
        if (name != other.name) return false
        return true
    }
    override fun hashCode(): Int {
        return name.hashCode()
    }
}

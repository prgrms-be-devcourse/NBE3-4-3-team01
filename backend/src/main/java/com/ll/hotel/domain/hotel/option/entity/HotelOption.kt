package com.ll.hotel.domain.hotel.option.entity

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToMany

@Entity
class HotelOption(
    name: String,

    @ManyToMany(mappedBy = "hotelOptions", fetch = FetchType.LAZY)
    val hotels: MutableSet<Hotel> = mutableSetOf()

) : BaseOption(name)
package com.ll.hotel.domain.hotel.option.entity

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany

@Entity
class HotelOption(
    name: String) : BaseOption(name) {
    @ManyToMany(mappedBy = "hotelOptions")
    var hotels: MutableSet<Hotel> = mutableSetOf()

    constructor():this("DEFAULT_NAME")
}
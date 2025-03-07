package com.ll.hotel.domain.hotel.option.entity

import com.ll.hotel.domain.hotel.room.entity.Room
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany

@Entity
class RoomOption(name: String): BaseOption(name) {
    @ManyToMany(mappedBy = "roomOptions")
    var rooms: MutableSet<Room> = mutableSetOf()

    constructor(): this("DEFAULT_NAME")
}
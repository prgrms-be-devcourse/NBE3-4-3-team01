package com.ll.hotel.domain.hotel.option.entity

import com.ll.hotel.domain.hotel.room.entity.Room
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany

@Entity
class RoomOption(
    name: String,

    @ManyToMany(mappedBy = "roomOptions")
    val rooms: MutableSet<Room> = mutableSetOf()

): BaseOption(name)
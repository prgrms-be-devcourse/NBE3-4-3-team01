package com.ll.hotel.domain.hotel.option.repository

import com.ll.hotel.domain.hotel.option.entity.RoomOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomOptionRepository: JpaRepository<RoomOption, Long> {
    fun findByNameIn(names: Collection<String>): List<RoomOption>
}
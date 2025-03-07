package com.ll.hotel.domain.hotel.option.repository

import com.ll.hotel.domain.hotel.option.entity.HotelOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HotelOptionRepository: JpaRepository<HotelOption, Long> {
    fun findByNameIn(names: Collection<String>): List<HotelOption>
}
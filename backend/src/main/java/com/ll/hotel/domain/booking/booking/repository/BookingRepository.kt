package com.ll.hotel.domain.booking.booking.repository

import com.ll.hotel.domain.booking.booking.entity.Booking
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.member.member.entity.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface BookingRepository : JpaRepository<Booking, Long> {
    fun findByMember(member: Member, pageable: Pageable): Page<Booking>
    fun findByHotel(hotel: Hotel, pageable: Pageable): Page<Booking>
}
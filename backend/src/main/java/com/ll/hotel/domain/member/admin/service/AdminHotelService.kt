package com.ll.hotel.domain.member.admin.service

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.member.admin.dto.request.AdminHotelRequest
import com.ll.hotel.global.exceptions.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminHotelService(
    private val hotelRepository: HotelRepository
) {
    fun findAllPaged(page: Int): Page<Hotel> {
        val pageable = PageRequest.of(page, 10)

        val pagedHotel = hotelRepository.findAll(pageable)

        if (pagedHotel.hasContent().not()) {
            ErrorCode.PAGE_NOT_FOUND.throwServiceException()
        }

        return pagedHotel
    }

    fun findById(id: Long): Hotel = hotelRepository.findById(id)
        .orElseThrow { ErrorCode.HOTEL_NOT_FOUND.throwServiceException() }

    @Transactional
    fun approve(hotel: Hotel, adminHotelRequest: AdminHotelRequest) {
        hotel.hotelStatus = adminHotelRequest.hotelStatus
    }

    fun flush() = hotelRepository.flush()
}
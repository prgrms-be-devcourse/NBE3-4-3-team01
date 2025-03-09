package com.ll.hotel.domain.hotel.option.service

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest
import com.ll.hotel.domain.hotel.option.dto.response.OptionResponse
import com.ll.hotel.domain.hotel.option.entity.HotelOption
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository
import com.ll.hotel.global.exceptions.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class HotelOptionService(
    private val hotelOptionRepository: HotelOptionRepository
) {
    @Transactional
    fun add(optionRequest: OptionRequest): OptionResponse {
        val hotelOption: HotelOption = hotelOptionRepository.save(HotelOption(optionRequest.name))
        return OptionResponse.from(hotelOption)
    }

    fun findAll(): List<OptionResponse> =
        hotelOptionRepository.findAll().map { OptionResponse.from(it) }

    fun findById(id: Long): HotelOption = hotelOptionRepository.findById(id)
        .orElseThrow(ErrorCode.HOTEL_OPTION_NOT_FOUND::throwServiceException)

    @Transactional
    fun modify(id: Long, optionRequest: OptionRequest): OptionResponse {
        val hotelOption = findById(id)
        hotelOption.name = optionRequest.name
        hotelOptionRepository.flush()
        return OptionResponse.from(hotelOption)
    }

    fun delete(id: Long) {
        val hotelOption = findById(id)

        if (hotelOption.hotels.isNotEmpty()) {
            ErrorCode.OPTION_IN_USE.throwServiceException()
        }
        hotelOptionRepository.delete(hotelOption)
    }
}
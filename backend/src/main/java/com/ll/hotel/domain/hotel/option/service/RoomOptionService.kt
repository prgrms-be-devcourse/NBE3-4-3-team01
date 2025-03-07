package com.ll.hotel.domain.hotel.option.service

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest
import com.ll.hotel.domain.hotel.option.dto.response.OptionResponse
import com.ll.hotel.domain.hotel.option.entity.RoomOption
import com.ll.hotel.domain.hotel.option.repository.RoomOptionRepository
import com.ll.hotel.global.exceptions.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class RoomOptionService(
    private val roomOptionRepository: RoomOptionRepository
) {
    @Transactional
    fun add(optionRequest: OptionRequest): OptionResponse {
        val roomOption: RoomOption = roomOptionRepository.save(RoomOption(optionRequest.name))
        return OptionResponse.from(roomOption)
    }

    fun findAll(): List<OptionResponse> =
        roomOptionRepository.findAll().map { OptionResponse.from(it) }

    fun findById(id: Long): RoomOption = roomOptionRepository.findById(id)
        .orElseThrow(ErrorCode.ROOM_OPTION_NOT_FOUND::throwServiceException)

    @Transactional
    fun modify(id: Long, optionRequest: OptionRequest): OptionResponse {
        val roomOption = findById(id)
        roomOption.name = optionRequest.name
        roomOptionRepository.flush()
        return OptionResponse.from(roomOption)
    }

    fun delete(id: Long) {
        val roomOption = findById(id)

        if (roomOption.rooms.isNotEmpty()) {
            ErrorCode.OPTION_IN_USE.throwServiceException()
        }
        roomOptionRepository.delete(roomOption)
    }
}
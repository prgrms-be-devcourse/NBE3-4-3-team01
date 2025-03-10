package com.ll.hotel.domain.member.admin.service

import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.member.admin.dto.request.AdminHotelRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminHotelResponse
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.standard.page.dto.PageDto
import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminHotelService(
    private val hotelRepository: HotelRepository
) {
    fun findAllPaged(page: Int): PageDto<AdminHotelResponse.Summary> {
        val pageable = PageRequest.of(page, 10)
        val pagedHotel = hotelRepository.findAll(pageable)

        if (pagedHotel.hasContent().not()) {
            ErrorCode.PAGE_NOT_FOUND.throwServiceException()
        }

        val pagedHotelSummaries = pagedHotel.map(AdminHotelResponse.Summary::from)

        return PageDto(pagedHotelSummaries)
    }

    fun findById(id: Long): AdminHotelResponse.Detail {
        val hotel = hotelRepository.findById(id)
            .orElseThrow(ErrorCode.HOTEL_NOT_FOUND::throwServiceException)

        return AdminHotelResponse.Detail.from(hotel)
    }

    @Transactional
    fun approve(id: Long, adminHotelRequest: AdminHotelRequest
    ): AdminHotelResponse.ApprovalResult {
        val hotel = hotelRepository.findById(id)
            .orElseThrow(ErrorCode.HOTEL_NOT_FOUND::throwServiceException)

        hotel.hotelStatus = adminHotelRequest.hotelStatus
        hotelRepository.flush()

        return AdminHotelResponse.ApprovalResult.from(hotel)
    }
}
package com.ll.hotel.domain.hotel.hotel.repository

import com.ll.hotel.domain.hotel.hotel.dto.HotelWithImageDto
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.domain.member.member.entity.Business
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface HotelRepository : JpaRepository<Hotel, Long> {
    @Query(
        """
        SELECT new com.ll.hotel.domain.hotel.hotel.dto.HotelWithImageDto(h, i)
        FROM Hotel h
        LEFT JOIN Image i
        ON i.referenceId = h._id
        AND i.imageType = :imageType
        WHERE h.hotelStatus <> 'UNAVAILABLE'
        AND (i.createdAt = (
        SELECT MIN(i2.createdAt)
        FROM Image i2
        WHERE i2.referenceId = h._id
        AND i2.imageType = :imageType
        )
        OR i is NULL)
        AND h.streetAddress LIKE %:streetAddress%
        """
    )
    fun findAllHotels(
        @Param("imageType") imageType: ImageType,
        @Param("streetAddress") streetAddress: String,
        pageRequest: PageRequest
    ): Page<HotelWithImageDto>

    @Query(
        """
        SELECT h
        FROM Hotel h
        WHERE h._id = :hotelId
        AND h.hotelStatus <> 'UNAVAILABLE'
        """
    )
    fun findHotelDetail(@Param("hotelId") hotelId: Long): Hotel?

    fun existsByHotelEmailAndIdNot(hotelEmail: String, hotelId: Long): Boolean

    fun findByBusiness(business: Business): Hotel?
}
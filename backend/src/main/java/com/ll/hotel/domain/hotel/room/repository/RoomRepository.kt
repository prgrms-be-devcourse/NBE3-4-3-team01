package com.ll.hotel.domain.hotel.room.repository

import com.ll.hotel.domain.hotel.room.dto.RoomWithImageDto
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.image.type.ImageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface RoomRepository : JpaRepository<Room, Long> {
    @Query(
        """
        SELECT new com.ll.hotel.domain.hotel.room.dto.RoomWithImageDto(r, i)
        FROM Room r
        LEFT JOIN Image i
        ON i.referenceId = r._id
        AND i.imageType = :imageType
        WHERE r.hotel._id = :hotelId
        AND r.roomStatus <> 'UNAVAILABLE'
        AND (
        i.createdAt = (
            SELECT MIN(i2.createdAt)
            FROM Image i2
            WHERE i2.referenceId = r._id
            AND i2.imageType = :imageType
        )
        OR i IS NULL
        )
        """
    )
    fun findAllRooms(@Param("hotelId") hotelId: Long, @Param("imageType") imageType: ImageType): List<RoomWithImageDto>

    @Query(
        """
        SELECT new com.ll.hotel.domain.hotel.room.dto.RoomWithImageDto(r, i)
        FROM Room r
        LEFT JOIN Image i
        ON i.referenceId = r._id
        AND i.imageType = :imageType
        WHERE r.hotel._id = :hotelId
        AND r.roomStatus <> 'UNAVAILABLE'
        AND (
        r.standardNumber <= :personal
        AND r.maxNumber >= :personal
        )
        AND (
        i.createdAt = (
            SELECT MIN(i2.createdAt)
            FROM Image i2
            WHERE i2.referenceId = r._id
            AND i2.imageType = :imageType
        )
        OR i IS NULL
        )
        """
    )
    fun findAllAvailableRooms(
        @Param("hotelId") hotelId: Long, @Param("imageType") imageType: ImageType, @Param("personal") personal: Int
    ): List<RoomWithImageDto>

    @Query(
        """ 
        SELECT r
        FROM Room r
        WHERE r.hotel._id = :hotelId
        And r._id = :roomId
        AND r.roomStatus <> 'UNAVAILABLE'
        """
    )
    fun findRoomDetail(@Param("hotelId") hotelId: Long, @Param("roomId") roomId: Long): Room?

    fun existsByHotelIdAndRoomNameAndIdNot(hotelId: Long, roomName: String, roomId: Long): Boolean
}
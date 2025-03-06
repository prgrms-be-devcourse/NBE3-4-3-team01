package com.ll.hotel.domain.hotel.hotel.repository;

import com.ll.hotel.domain.hotel.hotel.dto.HotelWithImageDto;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.domain.member.member.entity.Business;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @Query("""
            SELECT new com.ll.hotel.domain.hotel.hotel.dto.HotelWithImageDto(h, i)
            FROM Hotel h
            LEFT JOIN Image i
            ON i.referenceId = h.id
            AND i.imageType = :imageType
            WHERE h.hotelStatus <> 'UNAVAILABLE'
            AND (i.createdAt = (
                SELECT MIN(i2.createdAt)
                FROM Image i2
                WHERE i2.referenceId = h.id
                AND i2.imageType = :imageType
            )
            OR i is NULL)
            AND h.streetAddress LIKE %:streetAddress%
            """)
    Page<HotelWithImageDto> findAllHotels(@Param("imageType") ImageType imageType,
                                          @Param("streetAddress") String streetAddress, PageRequest pageRequest);

    @Query("""
            SELECT h
            FROM Hotel h
            WHERE h.id = :hotelId
            AND h.hotelStatus <> 'UNAVAILABLE'
            """)
    Optional<Hotel> findHotelDetail(@Param("hotelId") long hotelId);

    boolean existsByHotelEmailAndIdNot(String hotelEmail, long hotelId);

    Optional<Hotel> findByBusiness(Business business);
}

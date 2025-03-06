package com.ll.hotel.domain.booking.booking.repository;

import com.ll.hotel.domain.booking.booking.entity.Booking;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByMember(Member member, Pageable pageable);
    Page<Booking> findByHotel(Hotel hotel, Pageable pageable);
}
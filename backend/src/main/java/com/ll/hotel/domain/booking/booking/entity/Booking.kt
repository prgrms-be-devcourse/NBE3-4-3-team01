package com.ll.hotel.domain.booking.booking.entity

import com.ll.hotel.domain.booking.booking.type.BookingStatus
import com.ll.hotel.domain.booking.payment.entity.Payment
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.global.jpa.entity.BaseTime
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Booking(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    var room: Room,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    var hotel: Hotel,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member,

    @OneToOne
    @JoinColumn(name = "payment_id")
    var payment: Payment,

    @Column
    var bookingNumber: String,

    @Enumerated(EnumType.STRING)
    @Column
    var bookingStatus: BookingStatus,

    @Column
    var checkInDate: LocalDate,

    @Column
    var checkOutDate: LocalDate
) : BaseTime() {

    constructor(
        room: Room,
        hotel: Hotel,
        member: Member,
        payment: Payment,
        checkInDate: LocalDate,
        checkOutDate: LocalDate
    ) : this(
        room,
        hotel,
        member,
        payment,
        "", // 기본 bookingNumber
        BookingStatus.CONFIRMED, // 기본 bookingStatus
        checkInDate,
        checkOutDate
    )

    fun isReservedBy(member: Member): Boolean {
        return this.member == member
    }

    fun isOwnedBy(member: Member): Boolean {
        return member.isBusiness() && hotel.isOwnedBy(member)
    }
}
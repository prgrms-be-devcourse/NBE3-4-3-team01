package com.ll.hotel.domain.booking.booking.entity;

import com.ll.hotel.domain.booking.booking.type.BookingStatus;
import com.ll.hotel.domain.booking.payment.entity.Payment;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseTime {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name =  "member_id")
    private Member member;

    @NotNull
    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @NotNull
    @Column
    @Builder.Default
    private String bookingNumber = "";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.CONFIRMED;

    @NotNull
    @Column
    private LocalDate checkInDate;

    @NotNull
    @Column
    private LocalDate checkOutDate;

    public boolean isReservedBy(Member member) {
        return this.member.equals(member);
    }

    public boolean isOwnedBy(Member member) {
        return member.isBusiness() && hotel.isOwnedBy(member);
    }
}
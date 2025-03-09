package com.ll.hotel.domain.hotel.room.entity

import com.ll.hotel.domain.booking.booking.entity.Booking
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.option.entity.RoomOption
import com.ll.hotel.domain.hotel.room.dto.PostRoomRequest
import com.ll.hotel.domain.hotel.room.type.BedTypeNumber
import com.ll.hotel.domain.hotel.room.type.RoomStatus
import com.ll.hotel.global.jpa.entity.BaseTime
import jakarta.persistence.*

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "UK_hotel_roomName", columnNames = ["hotel_id", "room_name"])
    ]
)
class Room(
    @Column
    var roomName: String,

    @Column
    var roomNumber: Int,

    @Column
    var basePrice: Int,

    @Column
    var standardNumber: Int,

    @Column
    var maxNumber: Int,

    @Embedded
    var bedTypeNumber: BedTypeNumber,

    @Column
    @Enumerated(EnumType.STRING)
    var roomStatus: RoomStatus = RoomStatus.AVAILABLE,

    @ManyToOne(fetch = FetchType.LAZY)
    var hotel: Hotel,

    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    var bookings: MutableList<Booking> = mutableListOf(),

    @ManyToMany(cascade = [CascadeType.PERSIST])
    var roomOptions: MutableSet<RoomOption> = mutableSetOf()
) : BaseTime() {
    companion object {
        @JvmStatic  // 추후 제거
        fun roomBuild(
            hotel: Hotel,
            request: PostRoomRequest,
            bedTypeNumber: BedTypeNumber,
            roomOptions: MutableSet<RoomOption>
        ): Room {
            return Room(
                roomName = request.roomName,
                roomNumber = request.roomNumber,
                basePrice = request.basePrice,
                standardNumber = request.standardNumber,
                maxNumber = request.maxNumber,
                bedTypeNumber = bedTypeNumber,
                roomOptions = roomOptions,
                hotel = hotel
            )
        }
    }
}
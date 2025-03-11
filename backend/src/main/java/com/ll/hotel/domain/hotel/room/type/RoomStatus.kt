package com.ll.hotel.domain.hotel.room.type

enum class RoomStatus(val value: String) {
    AVAILABLE("사용 가능"),
    IN_BOOKING("예약 중"),
    UNAVAILABLE("사용 불가")
}
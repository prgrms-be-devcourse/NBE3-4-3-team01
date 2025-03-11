package com.ll.hotel.domain.hotel.hotel.type

enum class HotelStatus(val value: String) {
    AVAILABLE("사용 가능"),
    PENDING("승인 대기 중"),
    UNAVAILABLE("사용 불가")
}
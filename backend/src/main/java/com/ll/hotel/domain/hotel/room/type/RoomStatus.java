package com.ll.hotel.domain.hotel.room.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoomStatus {
    AVAILABLE("사용 가능"),
    IN_BOOKING("예약 중"),
    UNAVAILABLE("사용 불가");

    private final String value;
}

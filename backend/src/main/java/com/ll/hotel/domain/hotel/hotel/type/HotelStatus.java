package com.ll.hotel.domain.hotel.hotel.type;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HotelStatus {
    AVAILABLE("사용 가능"),
    PENDING("승인 대기 중"),
    UNAVAILABLE("사용 불가");

    private final String value;
}

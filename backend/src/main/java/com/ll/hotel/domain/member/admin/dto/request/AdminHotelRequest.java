package com.ll.hotel.domain.member.admin.dto.request;

import com.ll.hotel.domain.hotel.hotel.type.HotelStatus;
import jakarta.validation.constraints.NotNull;

public record AdminHotelRequest(
        @NotNull(message = "호텔 승인 상태는 필수 항목입니다.")
        HotelStatus hotelStatus
) {}

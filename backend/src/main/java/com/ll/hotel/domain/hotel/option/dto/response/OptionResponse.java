package com.ll.hotel.domain.hotel.option.dto.response;

import com.ll.hotel.domain.hotel.option.entity.BaseOption;

public record OptionResponse(Long optionId, String name) {
    public static OptionResponse from(BaseOption option) {
        return new OptionResponse(option.getId(), option.getName());
    }
}

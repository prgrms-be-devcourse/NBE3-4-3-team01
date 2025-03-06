package com.ll.hotel.domain.hotel.option.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OptionRequest(
        @NotBlank(message = "추가할 편의시설을 입력해주세요.")
        @Size(max = 255, message = "최대 255자까지 작성 가능합니다.")
        String name
) {}

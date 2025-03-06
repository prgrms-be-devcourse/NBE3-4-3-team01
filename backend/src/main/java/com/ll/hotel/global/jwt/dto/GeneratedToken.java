package com.ll.hotel.global.jwt.dto;

import lombok.Builder;

@Builder
public record GeneratedToken(
        String accessToken,
        String refreshToken
) {}
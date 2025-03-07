package com.ll.hotel.global.jwt.dto

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "custom.jwt")
data class JwtProperties(
    var secret: String = "",
    var accessTokenExpiration: Long = 0,
    var refreshTokenExpiration: Long = 0
) 
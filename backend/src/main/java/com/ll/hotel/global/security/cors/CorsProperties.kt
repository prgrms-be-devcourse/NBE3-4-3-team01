package com.ll.hotel.global.security.cors

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "custom.cors")
data class CorsProperties(
    var allowedOrigins: List<String> = emptyList()
) 
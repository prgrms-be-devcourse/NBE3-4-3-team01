package com.ll.hotel.global.security.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "custom.redis")
data class RedisProperties(
    var host: String = "",
    var port: Int = 0,
    var password: String = ""
) 
package com.ll.hotel.global.jwt.dto

import java.io.Serializable

import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import org.springframework.util.StringUtils

import jakarta.persistence.Id

@RedisHash(value = "rt")
class RefreshToken(
    @Id
    var id: String? = null,
    
    @Indexed
    var refreshToken: String? = null,
    
    var accessToken: String? = null,
    
    @TimeToLive
    var timeToLive: Long = 86400L
) : Serializable {

    constructor(id: String, refreshToken: String, accessToken: String) : this() {
        this.id = id
        this.refreshToken = refreshToken
        this.accessToken = accessToken
    }

    fun updateAccessToken(accessToken: String) {
        if (!StringUtils.hasText(accessToken)) {
            throw IllegalArgumentException("액세스 토큰은 null 이거나 비어 있을 수 없습니다")
        }
        this.accessToken = accessToken
    }
} 
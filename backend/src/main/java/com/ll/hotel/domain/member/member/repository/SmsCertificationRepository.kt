package com.ll.hotel.domain.member.member.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class SmsCertificationRepository(private val redisTemplate: StringRedisTemplate) {
    private val PREFIX = "sms:"
    private val LIMIT_TIME = 3 * 60 // 3분

    fun createSmsCertification(phone: String, certificationNumber: String) {
        redisTemplate.opsForValue()
            .set(PREFIX + phone, certificationNumber, Duration.ofSeconds(LIMIT_TIME.toLong()))
    }

    fun getSmsCertification(phone: String): String? {
        return redisTemplate.opsForValue().get(PREFIX + phone)
    }

    fun removeSmsCertification(phone: String) {
        redisTemplate.delete(PREFIX + phone)
    }

    fun hasKey(phone: String): Boolean {
        return redisTemplate.hasKey(PREFIX + phone) ?: false
    }
} 
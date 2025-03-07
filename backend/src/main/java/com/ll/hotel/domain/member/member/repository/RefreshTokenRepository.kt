package com.ll.hotel.domain.member.member.repository

import com.ll.hotel.global.jwt.dto.RefreshToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefreshTokenRepository : CrudRepository<RefreshToken, String> {
    fun findByRefreshToken(refreshToken: String): Optional<RefreshToken>
    fun findByAccessToken(accessToken: String): Optional<RefreshToken>
    fun existsByRefreshToken(refreshToken: String): Boolean
} 
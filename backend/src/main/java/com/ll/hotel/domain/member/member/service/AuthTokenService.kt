package com.ll.hotel.domain.member.member.service

import com.ll.hotel.global.jwt.dto.GeneratedToken
import com.ll.hotel.global.jwt.dto.JwtProperties
import com.ll.hotel.standard.util.Ut
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthTokenService(
    private val jwtProperties: JwtProperties,
    private val refreshTokenService: RefreshTokenService
) {
    private val log = LoggerFactory.getLogger(AuthTokenService::class.java)

    fun generateToken(email: String, role: String): GeneratedToken {
        val accessToken = genAccessToken(email, role)
        val refreshToken = refreshTokenService.generateRefreshToken(email)

        refreshTokenService.saveTokenInfo(email, refreshToken, accessToken)
        return GeneratedToken(accessToken, refreshToken)
    }

    fun genAccessToken(email: String, role: String): String {
        val claims: MutableMap<String, Any> = HashMap()
        claims["sub"] = email
        claims["role"] = role
        claims["type"] = "access"

        return Ut.jwt.toString(jwtProperties, claims)
    }

    fun verifyToken(token: String): Boolean {
        return try {
            val claims = Ut.jwt.getClaims(jwtProperties, token)
            claims.expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getEmail(token: String): String {
        return Ut.jwt.getClaims(jwtProperties, token).subject
    }
} 
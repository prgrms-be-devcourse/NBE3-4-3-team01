package com.ll.hotel.standard.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ll.hotel.global.app.AppConfig
import com.ll.hotel.global.jwt.dto.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import javax.crypto.SecretKey
import kotlin.collections.List

object Ut {
    private lateinit var SecretKey: SecretKey

    fun initSecretKey(jwtProperties : JwtProperties) {
        SecretKey = Keys.hmacShaKeyFor(
            jwtProperties.secret.toByteArray())
    }

    object Str {
        fun isBlank(str: String?) : Boolean = str.isNullOrBlank()
    }

    object Json {
        private val om: ObjectMapper = AppConfig.objectMapper

        fun toString(obj: Any): String = om.writeValueAsString(obj)

        fun toMap(jsonStr: String) :Map<String, Any>{
            return om.readValue(jsonStr, object : TypeReference<Map<String, Any>>() {})
        }
    }

    object Jwt {
        fun toString(jwtProperties: JwtProperties, claims: Map<String, Any>):String {
            val now = Date()
            val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray());

            val tokenType: String = claims["type"] as? String ?: "access"
            val expiration: Long = if (tokenType == "refresh") {
                jwtProperties.refreshTokenExpiration
            } else {
                jwtProperties.accessTokenExpiration
            }

            return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(Date(now.time + expiration))
                .signWith(secretKey)
                .compact()
        }

        fun getClaims(jwtProperties: JwtProperties, token: String): Claims {
            val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray());
            val cleanedToken = token.replace("Bearer ", "").trim()

            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(cleanedToken)
                .body
        }
    }

    object Random {
        private val characters = ('A'..'Z') + ('a'..'z') + ('0'..'9')

        fun generateUID(length: Int): String {
            return (1..length)
                .map { characters.random() }
                .joinToString("")

        }
    }

    object ListUt {
        fun <T> List<T>?.hasValue(): Boolean {
            return !this.isNullOrEmpty()
        }
    }
}
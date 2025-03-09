package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.repository.RefreshTokenRepository
import com.ll.hotel.global.exceptions.ErrorCode.*
import com.ll.hotel.global.jwt.dto.JwtProperties
import com.ll.hotel.global.jwt.dto.RefreshToken
import com.ll.hotel.global.response.RsData
import com.ll.hotel.standard.util.Ut
import io.jsonwebtoken.MalformedJwtException
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.TimeUnit

@Service
@Transactional(readOnly = true)
class RefreshTokenService(
    private val repository: RefreshTokenRepository,
    private val jwtProperties: JwtProperties,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val environment: Environment
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    
    private val isTestProfile: Boolean
        get() = environment.activeProfiles.any { it == "test" }

    @Transactional
    fun saveTokenInfo(email: String, refreshToken: String, accessToken: String) {
        try {
            log.debug("이메일 {}에 대한 토큰 저장 중", email)
            val token = RefreshToken(email, refreshToken, accessToken)
            repository.save(token)

            // 테스트 환경에서는 Redis 작업 건너뛰기
            if (isTestProfile) {
                log.debug("테스트 환경 감지됨, Redis 작업 건너뛰기")
                return
            }

            try {
                val key = "rt:refreshToken:$refreshToken"
                redisTemplate.opsForValue().set(key, email)
                redisTemplate.expire(key, jwtProperties.refreshTokenExpiration, TimeUnit.MILLISECONDS)
                log.debug("Redis에 토큰 저장 성공")
            } catch (e: RedisConnectionFailureException) {
                log.warn("Redis 연결 실패, 계속 진행합니다: {}", e.message)
            } catch (e: Exception) {
                log.warn("Redis에 토큰 저장 중 오류 발생, 계속 진행합니다: {}", e.message)
            }
        } catch (e: Exception) {
            log.error("저장소에 토큰 저장 중 오류 발생: {}", e.message)
            throw e
        }
    }

    fun generateRefreshToken(email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.refreshTokenExpiration)

        val claims: MutableMap<String, Any> = HashMap()
        claims["sub"] = email
        claims["exp"] = expiryDate
        claims["role"] = "ROLE_USER"
        claims["type"] = "refresh"
        return Ut.jwt.toString(jwtProperties, claims)
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): RsData<String> {
        try {
            log.debug("리프레시 토큰 {}으로 액세스 토큰 갱신 시도 중", refreshToken)
            val token = refreshToken.replace("Bearer ", "").trim()

            val tokenType = Ut.jwt.getClaims(jwtProperties, token).get("type", String::class.java)
            if (tokenType != "refresh") {
                log.debug("유효하지 않은 토큰 타입: {}", tokenType)
                REFRESH_TOKEN_INVALID.throwServiceException()
            }

            val tokenExists = repository.existsByRefreshToken(token)
            log.debug("저장소에 토큰 존재 여부: {}", tokenExists)

            if (!tokenExists) {
                log.debug("저장소에서 리프레시 토큰을 찾을 수 없음")
                REFRESH_TOKEN_NOT_FOUND.throwServiceException()
            }
            
            val resultToken = repository.findByRefreshToken(token)
                .orElseThrow { 
                    log.debug("저장소에서 리프레시 토큰을 찾을 수 없음 (findByRefreshToken)")
                    REFRESH_TOKEN_NOT_FOUND.throwServiceException() 
                }

            val email = resultToken.id
            log.debug("토큰에서 이메일 찾음: {}", email)
            
            val role = Ut.jwt.getClaims(jwtProperties, token).get("role", String::class.java)
            val newAccessToken = Ut.jwt.toString(jwtProperties, mapOf("sub" to email, "role" to role))

            resultToken.updateAccessToken(newAccessToken)
            repository.save(resultToken)
            log.debug("액세스 토큰 갱신 성공")

            return RsData.success(HttpStatus.OK, newAccessToken)
        } catch (e: MalformedJwtException) {
            log.error("잘못된 형식의 JWT: {}", e.message)
            throw TOKEN_INVALID.throwServiceException()
        } catch (e: Exception) {
            log.error("액세스 토큰 갱신 중 오류 발생: {}", e.message, e)
            throw INTERNAL_SERVER_ERROR.throwServiceException()
        }
    }

    @Transactional
    fun removeRefreshToken(email: String) {
        try {
            log.debug("이메일 {}에 대한 리프레시 토큰 삭제 중", email)
            repository.findById(email).ifPresent { repository.delete(it) }
            
            // 테스트 환경에서는 Redis 작업 건너뛰기
            if (isTestProfile) {
                log.debug("테스트 환경 감지됨, Redis 정리 작업 건너뛰기")
                return
            }
            
            try {
                // Redis에서도 관련 키 삭제 시도
                val pattern = "rt:refreshToken:*"
                val keys = redisTemplate.keys(pattern)
                if (keys.isNotEmpty()) {
                    redisTemplate.delete(keys)
                    log.debug("이메일 {}에 대한 Redis 키 삭제 완료", email)
                }
            } catch (e: Exception) {
                log.warn("Redis 키 정리 중 오류 발생, 계속 진행합니다: {}", e.message)
            }
        } catch (e: Exception) {
            log.error("리프레시 토큰 삭제 중 오류 발생: {}", e.message)
        }
    }
}
package com.ll.hotel.global.jwt.dto

data class GeneratedToken(
    val accessToken: String,
    val refreshToken: String
) {
    // 테스트 코드용 (추후 코틀린 마이그레이션 끝난 뒤 삭제)
    fun accessToken(): String = accessToken
    fun refreshToken(): String = refreshToken
    
    companion object {
        // Java 호환성을 위한 빌더 패턴 유지
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
        
        // 기존 빌더 패턴 방식 대신 코틀린 스타일의 팩토리 메서드 추가 (토큰 생성할 때 사용 예정)
        fun create(accessToken: String, refreshToken: String): GeneratedToken {
            return GeneratedToken(accessToken, refreshToken)
        }
    }

    class Builder {
        private var accessToken: String = ""
        private var refreshToken: String = ""

        fun accessToken(accessToken: String): Builder {
            this.accessToken = accessToken
            return this
        }

        fun refreshToken(refreshToken: String): Builder {
            this.refreshToken = refreshToken
            return this
        }

        fun build(): GeneratedToken {
            return GeneratedToken(accessToken, refreshToken)
        }
    }
} 
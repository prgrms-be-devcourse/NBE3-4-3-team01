package com.ll.hotel.global.security.oauth2.repository

import com.ll.hotel.global.security.oauth2.entity.OAuth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OAuthRepository : JpaRepository<OAuth, Long> {
    @Query("SELECT o FROM OAuth o JOIN FETCH o.member WHERE o.provider = :provider AND o.oauthId = :oauthId")
    fun findByProviderAndOauthIdWithMember(
        @Param("provider") provider: String,
        @Param("oauthId") oauthId: String
    ): Optional<OAuth>
    
    fun findByProviderAndOauthId(provider: String, oauthId: String): Optional<OAuth>
    
    fun existsByProviderAndOauthId(provider: String, oauthId: String): Boolean
} 
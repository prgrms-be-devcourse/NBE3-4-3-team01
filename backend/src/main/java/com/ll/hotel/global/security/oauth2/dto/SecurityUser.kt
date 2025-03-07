package com.ll.hotel.global.security.oauth2.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.core.user.OAuth2User

class SecurityUser : User, OAuth2User {
    val id: Long
    val email: String
    val provider: String?
    val isNewUser: Boolean
    private val attributesMap: Map<String, Any>?
    val oauthId: String?

    // 일반 로그인용 생성자
    constructor(id: Long, username: String, email: String, role: String) : 
        super(username, "", listOf(SimpleGrantedAuthority(role))) {
        this.id = id
        this.email = email
        this.provider = null
        this.isNewUser = false
        this.attributesMap = null
        this.oauthId = null
    }

    // OAuth2 로그인용 생성자
    constructor(
        email: String, 
        username: String, 
        provider: String,
        isNewUser: Boolean, 
        attributes: Map<String, Any>?,
        authorities: Collection<GrantedAuthority>, 
        oauthId: String
    ) : super(username, "", authorities) {
        this.id = -1
        this.email = email
        this.provider = provider
        this.isNewUser = isNewUser
        this.attributesMap = attributes
        this.oauthId = oauthId
    }

    companion object {
        @JvmStatic
        fun of(id: Long, username: String, email: String, role: String): SecurityUser {
            return SecurityUser(id, username, email, role)
        }
    }

    override fun getAttributes(): Map<String, Any>? {
        return attributesMap
    }

    override fun getName(): String {
        return email
    }
} 
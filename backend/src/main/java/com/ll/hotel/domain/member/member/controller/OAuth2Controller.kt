package com.ll.hotel.domain.member.member.controller

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.global.request.Rq
import com.ll.hotel.global.response.RsData
import com.ll.hotel.global.security.oauth2.entity.OAuth
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class OAuth2Controller(
    private val rq: Rq
) {
    @GetMapping("/oauth2/callback") // 사용하지 않는 컨트롤러 (임시 테스트를 위해 일단 보류)
    fun callback(
        @RequestParam(required = false) accessToken: String?,
        @RequestParam(required = false) refreshToken: String?,
        @RequestParam status: String
    ): RsData<OAuth2Response> {
        if ("SUCCESS" != status) {
            return RsData.success(HttpStatus.BAD_REQUEST, OAuth2Response(
                null, null, status, null, null, null,
                false, false, false, null, null
            ))
        }

        val actor = rq.actor
        if (actor == null) {
            return RsData.success(HttpStatus.UNAUTHORIZED, OAuth2Response(
                null, null, status, null, null, null,
                false, false, false, null, null
            ))
        }

        val oAuth = actor.getFirstOAuth()
        if (oAuth == null) {
            return RsData.success(HttpStatus.BAD_REQUEST, OAuth2Response(
                actor.memberEmail, actor.memberName, status,
                actor.getUserRole(), null, null,
                actor.isUser, actor.isAdmin, actor.isBusiness,
                accessToken, refreshToken
            ))
        }

        val response = OAuth2Response(
            actor.memberEmail,
            actor.memberName,
            status,
            actor.getUserRole(),
            oAuth.provider,
            oAuth.oauthId,
            actor.isUser,
            actor.isAdmin,
            actor.isBusiness,
            accessToken,
            refreshToken
        )

        return RsData.success(HttpStatus.OK, response)
    }
}

data class OAuth2Response(
    val email: String?,
    val name: String?,
    val status: String,
    val role: String?,
    val provider: String?,
    val oauthId: String?,
    val isUser: Boolean,
    val isAdmin: Boolean,
    val isBusiness: Boolean,
    val accessToken: String?,
    val refreshToken: String?
) 
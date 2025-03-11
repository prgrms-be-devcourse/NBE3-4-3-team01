package com.ll.hotel.global.security.oauth2

import com.ll.hotel.domain.member.member.service.AuthTokenService
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.global.jwt.dto.JwtProperties
import com.ll.hotel.global.security.oauth2.dto.SecurityUser
import com.ll.hotel.standard.util.CookieUtil
import com.ll.hotel.standard.util.Ut
import com.ll.hotel.standard.util.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class CustomOAuth2AuthenticationSuccessHandler(
    private val authTokenService: AuthTokenService,
    private val memberService: MemberService,
    private val jwtProperties: JwtProperties
) : AuthenticationSuccessHandler {

    private val log = logger()

    @Value("\${app.oauth2.authorizedRedirectUris}")
    private lateinit var authorizedRedirectUri: String

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest, 
        response: HttpServletResponse, 
        authentication: Authentication
    ) {
        val securityUser = authentication.principal as SecurityUser
        log.debug(
            "OAuth2 login success - provider: {}, oauthId: {}, email: {}", 
            securityUser.provider, securityUser.oauthId, securityUser.email
        )
        
        if (securityUser.isNewUser) {
            val provider = securityUser.provider ?: throw IllegalStateException("Provider는 null일 수 없습니다")
            val oauthId = securityUser.oauthId ?: throw IllegalStateException("OAuthId는 null일 수 없습니다")
            
            val redirectUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("provider", URLEncoder.encode(provider, StandardCharsets.UTF_8))
                .queryParam("oauthId", URLEncoder.encode(oauthId, StandardCharsets.UTF_8))
                .queryParam("status", "REGISTER")
                .build()
                .encode()
                .toUriString()

            response.sendRedirect(redirectUrl)
        } else {
            val provider = securityUser.provider ?: throw IllegalStateException("Provider는 null일 수 없습니다")
            val oauthId = securityUser.oauthId ?: throw IllegalStateException("OAuthId는 null일 수 없습니다")
            
            val member = memberService.findByProviderAndOauthId(provider, oauthId)
            val accessToken = authTokenService.generateToken(member.memberEmail, member.getUserRole()).accessToken
            val refreshToken = memberService.generateRefreshToken(member.memberEmail)
            log.debug("Generated JWT access token: {}", accessToken)
            log.debug("Generated JWT refresh token: {}", refreshToken)

            val roleData = mutableMapOf<String, Any>()
            roleData["role"] = member.getUserRole()
            if (member.getUserRole() == "BUSINESS") {
                val hotel = member.business?.hotel
                if (hotel != null) {
                    roleData["hasHotel"] = true
                    roleData["hotelId"] = hotel.id
                }
            }
            val encodedRoleData = URLEncoder.encode(Ut.Json.toString(roleData), StandardCharsets.UTF_8)

            CookieUtil.addCookie(
                response, 
                "role", 
                encodedRoleData, 
                (jwtProperties.accessTokenExpiration / 1000).toInt(), 
                false, 
                true
            )
            
            CookieUtil.addCookie(
                response, 
                "access_token", 
                accessToken, 
                (jwtProperties.accessTokenExpiration / 1000).toInt(), 
                true, 
                true
            )
            
            val redirectUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("status", "SUCCESS")
                .build()
                .toUriString()
            
            CookieUtil.addCookie(
                response, 
                "refresh_token", 
                refreshToken, 
                (jwtProperties.refreshTokenExpiration / 1000).toInt(), 
                true, 
                true
            )
            
            response.sendRedirect(redirectUrl)
        }
    }
} 
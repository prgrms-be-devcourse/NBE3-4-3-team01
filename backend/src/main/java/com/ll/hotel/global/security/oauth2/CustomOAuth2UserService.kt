package com.ll.hotel.global.security.oauth2

import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.global.security.oauth2.dto.SecurityUser
import com.ll.hotel.global.security.oauth2.repository.OAuthRepository
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService,
    private val oAuthRepository: OAuthRepository
) : DefaultOAuth2UserService() {

    private val log = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        log.debug("OAuth2 로그인 시도")
        log.debug("Provider: {}", userRequest.clientRegistration.registrationId)
        log.debug("Access Token: {}", userRequest.accessToken.tokenValue)
        log.debug("Additional Parameters: {}", userRequest.additionalParameters)

        return try {
            val oauth2User = super.loadUser(userRequest)
            log.debug("OAuth2User Attributes: {}", oauth2User.attributes)
            processOAuth2User(userRequest, oauth2User)
        } catch (e: Exception) {
            throw OAuth2AuthenticationException("OAuth2 로그인 처리 중 오류가 발생했습니다.")
        }
    }

    private fun processOAuth2User(userRequest: OAuth2UserRequest, oauth2User: OAuth2User): OAuth2User {
        val registrationId = userRequest.clientRegistration.registrationId
        
        val (email, name, oauthId) = when (registrationId) {
            "naver" -> {
                val response = oauth2User.attributes["response"] as? Map<String, Any>
                    ?: throw OAuth2AuthenticationException("Invalid Naver response")
                val email = response["email"] as String
                val name = response["name"] as String
                val oauthId = response["id"] as String
                log.debug("네이버 로그인 정보 - email: {}, name: {}, id: {}", email, name, oauthId)
                Triple(email, name, oauthId)
            }
            "kakao" -> {
                val kakaoAccount = oauth2User.attributes["kakao_account"] as Map<String, Any>
                val profile = kakaoAccount["profile"] as Map<String, Any>
                val email = kakaoAccount["email"] as String
                val name = profile["nickname"] as String
                val oauthId = oauth2User.attributes["id"].toString()
                log.debug("카카오 로그인 정보 - email: {}, name: {}, id: {}", email, name, oauthId)
                Triple(email, name, oauthId)
            }
            "google" -> {
                val email = oauth2User.getAttribute<String>("email")
                val name = oauth2User.getAttribute<String>("name")
                val oauthId = oauth2User.getAttribute<String>("sub")
                log.debug("구글 로그인 정보 - email: {}, name: {}, id: {}", email, name, oauthId)
                Triple(email, name, oauthId)
            }
            else -> throw OAuth2AuthenticationException("Unsupported provider: $registrationId")
        }

        if (email == null || name == null || oauthId == null) {
            throw OAuth2AuthenticationException("Required attributes are missing")
        }

        val isNewUser = !oAuthRepository.existsByProviderAndOauthId(registrationId, oauthId)
        
        return SecurityUser(
            email,
            name,
            registrationId,
            isNewUser,
            oauth2User.attributes,
            oauth2User.authorities,
            oauthId
        )
    }
} 
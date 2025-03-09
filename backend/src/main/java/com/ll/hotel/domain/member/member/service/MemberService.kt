package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.member.member.dto.FavoriteDto
import com.ll.hotel.domain.member.member.dto.JoinRequest
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ErrorCode.*
import com.ll.hotel.global.jwt.dto.GeneratedToken
import com.ll.hotel.global.jwt.dto.JwtProperties
import com.ll.hotel.global.request.Rq
import com.ll.hotel.global.response.RsData
import com.ll.hotel.global.security.oauth2.entity.OAuth
import com.ll.hotel.global.security.oauth2.repository.OAuthRepository
import com.ll.hotel.standard.util.Ut
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val oAuthRepository: OAuthRepository,
    private val authTokenService: AuthTokenService,
    private val refreshTokenService: RefreshTokenService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val jwtProperties: JwtProperties,
    private val hotelRepository: HotelRepository,
    private val rq: Rq
) {
    companion object {
        private val log = LoggerFactory.getLogger(MemberService::class.java)
        private const val LOGOUT_PREFIX = "LOGOUT:"
    }

    @Transactional
    fun join(@Valid joinRequest: JoinRequest): Member {
        log.debug("Join service - OAuth 정보: provider={}, oauthId={}", 
                 joinRequest.provider, joinRequest.oauthId)
                 
        val existingMember = memberRepository.findByMemberEmail(joinRequest.email)
        if (existingMember.isPresent) {
            val member = existingMember.get()
            
            // OAuth 정보가 있는 경우에만 저장
            if (joinRequest.provider.isNotBlank() && joinRequest.oauthId.isNotBlank()) {
                val oauth = OAuth.builder()
                        .member(member)
                        .provider(joinRequest.provider)
                        .oauthId(joinRequest.oauthId)
                        .build()
                oAuthRepository.save(oauth)
                member.oauths.add(oauth)
                return member
            }
            
            throw EMAIL_ALREADY_EXISTS.throwServiceException()
        }

        val newMember = Member.builder()
                .memberEmail(joinRequest.email)
                .memberName(joinRequest.name)
                .memberPhoneNumber(joinRequest.phoneNumber)
                .role(joinRequest.role)
                .memberStatus(MemberStatus.ACTIVE)
                .birthDate(joinRequest.birthDate)
                .build()
        
        val savedMember = memberRepository.save(newMember)
        
        val oauth = OAuth.builder()
                .member(savedMember)
                .provider(joinRequest.provider)
                .oauthId(joinRequest.oauthId)
                .build()
        oAuthRepository.save(oauth)
        
        return savedMember
    }

    fun findByMemberEmail(email: String): Optional<Member> {
        return memberRepository.findByMemberEmail(email)
    }

    fun existsByMemberEmail(email: String): Boolean {
        log.debug("Checking if member exists with email: {}", email)
        val exists = memberRepository.existsByMemberEmail(email)
        log.debug("Member exists with email {}: {}", email, exists)
        return exists
    }

    fun verifyToken(accessToken: String): Boolean {
        return authTokenService.verifyToken(accessToken)
    }

    fun refreshAccessToken(refreshToken: String): RsData<String> {
        return refreshTokenService.refreshAccessToken(refreshToken)
    }

    fun generateRefreshToken(email: String): String {
        return refreshTokenService.generateRefreshToken(email)
    }

    @Transactional
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        val cookies = request.cookies
        var accessToken: String? = null
        var refreshToken: String? = null
        
        if (cookies != null) {
            for (cookie in cookies) {
                when (cookie.name) {
                    "access_token" -> accessToken = cookie.value
                    "refresh_token" -> refreshToken = cookie.value
                }
            }
        }

        // 액세스 토큰이 만료되었다면 리프레시 토큰으로 처리
        if (accessToken == null && refreshToken != null) {
            val refreshResult = refreshAccessToken(refreshToken)
            if (refreshResult.isSuccess) {
                accessToken = refreshResult.data
            }
        }

        if (accessToken == null) {
            throw UNAUTHORIZED.throwServiceException()
        }

        val email = authTokenService.getEmail(accessToken)
        
        // Redis에서 토큰 무효화
        redisTemplate.opsForValue().set(
            LOGOUT_PREFIX + accessToken,
            email,
            jwtProperties.accessTokenExpiration,
            TimeUnit.MILLISECONDS
        )

        // Refresh 토큰 삭제
        refreshTokenService.removeRefreshToken(email)

        deleteCookie(response)
    }

    private fun deleteCookie(response: HttpServletResponse) {
        val accessTokenCookie = Cookie("access_token", null)
        accessTokenCookie.maxAge = 0
        accessTokenCookie.path = "/"

        val roleCookie = Cookie("role", null)
        roleCookie.maxAge = 0
        roleCookie.path = "/"

        val refreshTokenCookie = Cookie("refresh_token", null)
        refreshTokenCookie.maxAge = 0
        refreshTokenCookie.path = "/"

        val oauth2AuthRequestCookie = Cookie("oauth2_auth_request", null)
        oauth2AuthRequestCookie.maxAge = 0
        oauth2AuthRequestCookie.path = "/"

        response.addCookie(accessTokenCookie)
        response.addCookie(roleCookie)
        response.addCookie(refreshTokenCookie)
        response.addCookie(oauth2AuthRequestCookie)
    }

    fun isLoggedOut(token: String): Boolean {
        return redisTemplate.hasKey(LOGOUT_PREFIX + token) == true
    }

    fun getEmailFromToken(token: String): String {
        return authTokenService.getEmail(token)
    }

    fun extractEmailIfValid(token: String): String {
        if (isLoggedOut(token)) {
            throw TOKEN_LOGGED_OUT.throwServiceException()
        }
        if (!verifyToken(token)) {
            throw TOKEN_INVALID.throwServiceException()
        }
        return getEmailFromToken(token)
    }

    @Transactional
    fun addFavorite(hotelId: Long) {
        val actor = rq.actor
            ?: throw UNAUTHORIZED.throwServiceException()

        val hotel = hotelRepository.findById(hotelId)
            .orElseThrow { HOTEL_NOT_FOUND.throwServiceException() }

        if (actor.favoriteHotels.contains(hotel)) {
            throw FAVORITE_ALREADY_EXISTS.throwServiceException()
        }

        actor.favoriteHotels.add(hotel)
        hotel.favorites.add(actor)
        
        memberRepository.save(actor)
        hotelRepository.save(hotel)
    }

    @Transactional
    fun removeFavorite(hotelId: Long) {
        val actor = rq.actor
            ?: throw UNAUTHORIZED.throwServiceException()

        val hotel = hotelRepository.findById(hotelId)
            .orElseThrow { HOTEL_NOT_FOUND.throwServiceException() }

        if (!actor.favoriteHotels.contains(hotel)) {
            throw FAVORITE_NOT_FOUND.throwServiceException()
        }

        actor.favoriteHotels.remove(hotel)
        hotel.favorites.remove(actor)
        
        memberRepository.save(actor)
        hotelRepository.save(hotel)
    }

    fun getFavoriteHotels(): List<FavoriteDto> {
        val actor = rq.actor
        log.debug("getFavoriteHotels - actor: {}", actor)
        
        if (actor == null) {
            throw UNAUTHORIZED.throwServiceException()
        }

        val favorites = actor.favoriteHotels
        
        if (favorites.isEmpty()) {
            return emptyList()
        }
        
        return favorites.stream()
            .map { hotel -> FavoriteDto.from(hotel) }
            .collect(Collectors.toList())
    }
  
    fun isFavoriteHotel(hotelId: Long): Boolean {
        val actor = rq.actor
            ?: throw UNAUTHORIZED.throwServiceException()

        val favorites = actor.favoriteHotels

        return favorites.stream()
                .anyMatch { hotel -> hotel.id == hotelId }
    }

    @Transactional(readOnly = true)
    fun findByProviderAndOauthId(provider: String, oauthId: String): Member {
        return oAuthRepository.findByProviderAndOauthIdWithMember(provider, oauthId)
            .orElseThrow { OAUTH_NOT_FOUND.throwServiceException() }
            .member!!
    }

    @Transactional
    fun oAuth2Login(member: Member, response: HttpServletResponse) {
        val tokens = authTokenService.generateToken(
            member.memberEmail, 
            member.getUserRole()
        )
        
        addAuthCookies(response, tokens, member)
    }

    private fun addAuthCookies(response: HttpServletResponse, tokens: GeneratedToken, member: Member) {
        // Access Token 쿠키
        val accessTokenCookie = Cookie("access_token", tokens.accessToken)
        accessTokenCookie.path = "/"
        accessTokenCookie.isHttpOnly = true
        accessTokenCookie.maxAge = (jwtProperties.accessTokenExpiration / 1000).toInt()
        response.addCookie(accessTokenCookie)
        
        // Refresh Token 쿠키
        val refreshTokenCookie = Cookie("refresh_token", tokens.refreshToken)
        refreshTokenCookie.path = "/"
        refreshTokenCookie.isHttpOnly = true
        refreshTokenCookie.maxAge = (jwtProperties.refreshTokenExpiration / 1000).toInt()
        response.addCookie(refreshTokenCookie)
        
        // Role 정보 쿠키
        val roleData = HashMap<String, Any>()
        roleData["role"] = member.getUserRole()
        
        if (member.getUserRole() == "BUSINESS" && member.business?.hotel != null) {
            roleData["hasHotel"] = true
            roleData["hotelId"] = member.business!!.hotel!!.id
        }
        
        val encodedRoleData = URLEncoder.encode(
            Ut.json.toString(roleData),
            StandardCharsets.UTF_8
        )
        
        val roleCookie = Cookie("role", encodedRoleData)
        roleCookie.path = "/"
        roleCookie.maxAge = (jwtProperties.accessTokenExpiration / 1000).toInt()
        response.addCookie(roleCookie)
    }
} 
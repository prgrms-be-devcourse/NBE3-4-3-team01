package com.ll.hotel.global.jwt

import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.global.exceptions.ErrorCode.MEMBER_NOT_FOUND
import com.ll.hotel.global.exceptions.ErrorCode.TOKEN_EXPIRED
import com.ll.hotel.global.exceptions.ServiceException
import com.ll.hotel.global.security.oauth2.dto.SecurityUser.Companion.of
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthFilter(
    private val memberService: MemberService,
    private val memberRepository: MemberRepository
) : OncePerRequestFilter(), Ordered {

    private val log = LoggerFactory.getLogger(JwtAuthFilter::class.java)

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE - 100
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cookies = request.cookies
        var accessToken: String? = null
        var refreshToken: String? = null
        
        if (cookies != null) {
            for (cookie in cookies) {
                when (cookie.name) {
                    "access_token" -> {
                        accessToken = cookie.value
                        log.debug("Access Token 존재: {}", accessToken)
                    }
                    "refresh_token" -> {
                        refreshToken = cookie.value
                        log.debug("Refresh Token 존재: {}", refreshToken)
                    }
                }
            }
        }

        if (StringUtils.hasText(accessToken)) {
            try {
                val email = memberService.extractEmailIfValid(accessToken!!)
                log.debug("Access Token 유효함. Email: {}", email)

                val member = memberRepository.findByMemberEmail(email)
                    .orElseThrow { MEMBER_NOT_FOUND.throwServiceException() }
                log.debug("Found member: {}", member.memberEmail)

                val userDto = of(
                    member.id,
                    member.memberName,
                    member.memberEmail,
                    "ROLE_" + member.role
                )

                val auth: Authentication = UsernamePasswordAuthenticationToken(
                    userDto,
                    null,
                    userDto.authorities
                )
                SecurityContextHolder.getContext().authentication = auth
                
            } catch (e: ServiceException) {
                val errorMessage = e.message
                val resultCode = errorMessage?.split(" : ")?.get(0)
                log.debug("Token 검증 실패. Error Code: {}, Message: {}", resultCode, errorMessage)

                if (resultCode == "401-2" && StringUtils.hasText(refreshToken)) {
                    log.debug("Access Token 만료됨. Refresh Token으로 갱신 시도")
                    try {
                        val refreshResult = memberService.refreshAccessToken(refreshToken!!)
                        if (refreshResult.isSuccess) {
                            log.debug("새로운 Access Token 발급 성공")
                            val newAccessTokenCookie = Cookie("access_token", refreshResult.data)
                            newAccessTokenCookie.path = "/"
                            newAccessTokenCookie.isHttpOnly = true
                            response.addCookie(newAccessTokenCookie)
                            
                            val email = memberService.extractEmailIfValid(refreshResult.data!!)
                            val member = memberRepository.findByMemberEmail(email)
                                .orElseThrow { MEMBER_NOT_FOUND.throwServiceException() }

                            val userDto = of(
                                member.id,
                                member.memberName,
                                member.memberEmail,
                                "ROLE_" + member.role
                            )

                            val auth: Authentication = UsernamePasswordAuthenticationToken(
                                userDto,
                                null,
                                userDto.authorities
                            )
                            SecurityContextHolder.getContext().authentication = auth
                        }
                    } catch (refreshError: Exception) {
                        throw TOKEN_EXPIRED.throwServiceException()
                    }
                } else {
                    throw e
                }
            }
        }
        
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestURI = request.requestURI
        return requestURI == "/api/users/refresh" ||
               (requestURI.contains("/oauth2/callback") && request.getParameter("accessToken") == null) ||
               requestURI.startsWith("/h2-console")
    }
} 
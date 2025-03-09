package com.ll.hotel.global.jwt

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.service.AuthTokenService
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.domain.member.member.service.RefreshTokenService
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ErrorCode.UNAUTHORIZED
import com.ll.hotel.global.exceptions.ServiceException
import com.ll.hotel.global.jwt.dto.GeneratedToken
import com.ll.hotel.global.jwt.dto.JwtProperties
import com.ll.hotel.global.request.Rq
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Autowired
    private lateinit var memberService: MemberService
    
    @Autowired
    private lateinit var refreshTokenService: RefreshTokenService

    @Autowired
    private lateinit var jwtProperties: JwtProperties
    
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>
    
    @Mock
    private lateinit var rq: Rq
    
    private lateinit var testMember: Member
    private lateinit var generatedTokens: GeneratedToken

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // 레디스 초기화 - 모든 키 삭제
        redisTemplate.connectionFactory?.connection?.flushAll()

        // 테스트 멤버 생성
        testMember = Member.builder()
                .memberEmail("test@example.com")
                .memberName("테스트사용자")
                .memberPhoneNumber("010-1234-5678")
                .role(Role.USER)
                .memberStatus(MemberStatus.ACTIVE)
                .build()
        
        memberRepository.save(testMember)
        
        // 토큰 생성
        generatedTokens = authTokenService.generateToken(
                testMember.memberEmail, 
                testMember.role.toString()
        )
        
        // 리프레시 토큰 저장
        refreshTokenService.saveTokenInfo(
            testMember.memberEmail,
            generatedTokens.refreshToken,
            generatedTokens.accessToken
        )
        
        ReflectionTestUtils.setField(memberService, "rq", rq)
        
        `when`(rq.actor).thenReturn(testMember)
        
        SecurityContextHolder.clearContext()
    }

    @Test
    @DisplayName("쿠키에 액세스 토큰을 담아 API 호출")
    fun accessAPI_WithCookieToken() {
        // given
        val accessTokenCookie = Cookie("access_token", generatedTokens.accessToken)
        accessTokenCookie.path = "/"
        accessTokenCookie.isHttpOnly = true
        
        // when & then
        mockMvc.perform(get("/api/favorites/me")
               .cookie(accessTokenCookie))
               .andExpect(status().isOk)
    }
    
    @Test
    @DisplayName("토큰 없이 API 호출 시 401 에러")
    fun noToken_Unauthorized() {
        // 인증 실패 케이스 테스트
        `when`(rq.actor).thenThrow(ServiceException(UNAUTHORIZED.httpStatus, "로그인이 필요합니다."))
        
        // when & then
        mockMvc.perform(get("/api/favorites/me"))
               .andExpect(status().isUnauthorized)
    }
    
    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰 갱신 테스트")
    fun refreshToken_Success() {
        // given
        val refreshToken = generatedTokens.refreshToken

        // when
        val result = memberService.refreshAccessToken(refreshToken)
        
        // then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.data).isNotEmpty()
        
        // 새 토큰으로 이메일 추출 테스트
        val email = memberService.getEmailFromToken(result.data)
        assertThat(email).isEqualTo(testMember.memberEmail)
    }
    
    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    fun expiredToken_FailsValidation() {
        // given
        val expiredToken = generateExpiredToken()
        
        // when & then
        val isValid = memberService.verifyToken(expiredToken)
        assertThat(isValid).isFalse()
    }
    
    @Test
    @DisplayName("JWT 토큰 검증 성공 테스트")
    fun validateToken_Success() {
        // given
        val token = generatedTokens.accessToken
        
        // when
        val isValid = memberService.verifyToken(token)
        
        // then
        assertThat(isValid).isTrue()
    }
    
    @Test
    @DisplayName("토큰에서 이메일 추출 테스트")
    fun extractEmailFromToken() {
        // given
        val token = generatedTokens.accessToken
        
        // when
        val email = memberService.getEmailFromToken(token)
        
        // then
        assertThat(email).isEqualTo(testMember.memberEmail)
    }

    // 만료된 토큰 생성 도우미 메서드
    private fun generateExpiredToken(): String {
        val nowMillis = System.currentTimeMillis()
        val expMillis = nowMillis - 1000 // 1초 전에 만료됨
        
        return Jwts.builder()
                .setSubject(testMember.memberEmail)
                .claim("role", testMember.role)
                .claim("type", "access")
                .setIssuedAt(Date(nowMillis))
                .setExpiration(Date(expMillis))
                .signWith(Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray()))
                .compact()
    }
} 
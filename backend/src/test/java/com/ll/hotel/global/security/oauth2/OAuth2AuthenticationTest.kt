package com.ll.hotel.global.security.oauth2

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.request.Rq
import com.ll.hotel.global.security.oauth2.entity.OAuth
import com.ll.hotel.global.security.oauth2.repository.OAuthRepository
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
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
class OAuth2AuthenticationTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var oAuthRepository: OAuthRepository

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var rq: Rq

    private lateinit var testMember: Member
    private lateinit var testOAuth: OAuth
    private lateinit var response: MockHttpServletResponse

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        response = MockHttpServletResponse()

        // Rq 모킹 설정
        ReflectionTestUtils.setField(memberService, "rq", rq)

        // 테스트용 회원 생성
        testMember = Member.builder()
                .memberEmail("oauth2test@example.com")
                .memberName("OAuth2 테스트")
                .memberPhoneNumber("010-9876-5432")
                .role(Role.USER)
                .memberStatus(MemberStatus.ACTIVE)
                .oauths(ArrayList())
                .build()

        memberRepository.save(testMember)

        // 테스트용 OAuth 정보 생성
        testOAuth = OAuth.builder()
                .provider("google")
                .oauthId("123456789")
                .member(testMember)
                .build()

        testMember.oauths.add(testOAuth)
        oAuthRepository.save(testOAuth)
        memberRepository.save(testMember)

        `when`(rq.actor).thenReturn(testMember)
    }

    @Test
    @DisplayName("OAuth2 로그인 후 토큰 검증")
    fun validateTokenAfterOAuth2Login() {
        // given
        // OAuth2 로그인 프로세스
        memberService.oAuth2Login(testMember, response)

        // 토큰 추출
        val accessToken = response.getCookie("access_token")?.value
        val refreshToken = response.getCookie("refresh_token")?.value

        // when & then
        // 액세스 토큰 검증
        assertThat(memberService.verifyToken(accessToken.toString())).isTrue()
        // 리프레시 토큰 검증
        assertThat(memberService.verifyToken(refreshToken.toString())).isTrue()
    }

    @Test
    @DisplayName("다른 OAuth 제공자 계정 연결 성공")
    fun linkAnotherOAuthProvider_Success() {
        // given
        val provider = "kakao"
        val oauthId = "kakao12345"

        // when
        val linkedOAuth = OAuth.builder()
                .provider(provider)
                .oauthId(oauthId)
                .member(testMember)
                .build()

        testMember.oauths.add(linkedOAuth)
        oAuthRepository.save(linkedOAuth)
        memberRepository.save(testMember)

        // then
        val updatedMember = memberRepository.findById(testMember.id).orElseThrow()
        assertThat(updatedMember.oauths).hasSize(2)
        assertThat(updatedMember.oauths).anyMatch { oauth ->
            oauth.provider == provider && oauth.oauthId == oauthId
        }
    }
    
    @Test
    @DisplayName("OAuth 쿠키 기반 인증 테스트")
    fun cookieBasedAuthenticationTest() {
        // given
        memberService.oAuth2Login(testMember, response)
        val accessTokenCookie = response.getCookie("access_token")
        
        // when & then
        mockMvc.perform(get("/api/favorites/me")
                .cookie(accessTokenCookie))
                .andDo(print())
                .andExpect(status().isOk)
    }
    
    @Test
    @DisplayName("OAuth 인증 후 SecurityContext 설정 테스트")
    fun securityContextAfterOAuth2Login() {
        // given
        // 인증 정보 생성
        val auth = UsernamePasswordAuthenticationToken(
            testMember,
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
        
        // when
        SecurityContextHolder.getContext().authentication = auth
        
        // then
        val currentAuth = SecurityContextHolder.getContext().authentication
        assertThat(currentAuth).isNotNull
        assertThat(currentAuth.principal).isEqualTo(testMember)
    }

    @Test
    @DisplayName("OAuth 제공자로 회원 조회 테스트")
    fun findMemberByProviderAndOauthId() {
        // given
        val provider = "google"
        val oauthId = "123456789"
        
        // when
        val foundMember = memberService.findByProviderAndOauthId(provider, oauthId)
        
        // then
        assertThat(foundMember).isNotNull
        assertThat(foundMember.id).isEqualTo(testMember.id)
        assertThat(foundMember.memberEmail).isEqualTo(testMember.memberEmail)
    }
    
    @Test
    @DisplayName("OAuth 로그인 실패 - 유효하지 않은 제공자")
    fun oauthLoginFail_InvalidProvider() {
        // given
        val invalidProvider = "invalid-provider"
        val oauthId = "123456789"
        
        // when & then
        assertThat(oAuthRepository.findByProviderAndOauthIdWithMember(invalidProvider, oauthId))
            .isEmpty
    }
} 
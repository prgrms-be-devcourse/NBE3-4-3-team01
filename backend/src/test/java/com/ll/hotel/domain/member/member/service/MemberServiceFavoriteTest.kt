package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.dto.FavoriteDto
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ServiceException
import com.ll.hotel.global.request.Rq
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime
import java.util.HashSet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceFavoriteTest {

    @Autowired
    private lateinit var memberService: MemberService
    
    @Autowired
    private lateinit var memberRepository: MemberRepository
    
    @Autowired
    private lateinit var hotelRepository: HotelRepository
    
    @Mock
    private lateinit var rq: Rq
    
    private lateinit var testHotel: Hotel
    private lateinit var testMember: Member
    
    @BeforeEach
    fun setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this)
        
        // MemberService에 모의 Rq 주입
        ReflectionTestUtils.setField(memberService, "rq", rq)
        
        // 테스트 데이터 초기화
        testMember = Member.builder()
                .memberEmail("test@example.com")
                .memberName("Test User")
                .memberPhoneNumber("010-1234-5678")
                .role(Role.USER)
                .memberStatus(MemberStatus.ACTIVE)
                .favoriteHotels(HashSet())
                .build()
        
        memberRepository.save(testMember)
        
        testHotel = Hotel.builder()
                .hotelName("Test Hotel")
                .hotelEmail("hotel@example.com")
                .hotelPhoneNumber("02-123-4567")
                .streetAddress("123 Test St")
                .zipCode(12345)
                .hotelGrade(5)
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .hotelExplainContent("Test hotel description")
                .hotelStatus(HotelStatus.PENDING)
                .favorites(HashSet())
                .build()
        
        hotelRepository.save(testHotel)
        
        // Rq 모킹 설정
        Mockito.`when`(rq.actor).thenReturn(testMember)
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 - 성공")
    fun addFavorite_Success() {
        // when
        memberService.addFavorite(testHotel.id)
        
        // then
        val updatedMember = memberRepository.findByMemberEmail("test@example.com").orElseThrow()
        assertThat(updatedMember.favoriteHotels).contains(testHotel)
    }
    
    @Test
    @DisplayName("즐겨찾기 추가 실패 - 로그인하지 않은 경우")
    fun addFavorite_Fail_NotLoggedIn() {
        // 로그인하지 않은 상태 모킹
        Mockito.`when`(rq.actor).thenReturn(null)
        
        // when & then
        assertThatThrownBy { memberService.addFavorite(testHotel.id) }
                .isInstanceOf(ServiceException::class.java)
                .hasMessageContaining("로그인이 필요합니다")
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 실패 - 존재하지 않는 호텔")
    fun addFavorite_Fail_HotelNotFound() {
        // when & then
        assertThatThrownBy { memberService.addFavorite(9999L) }
                .isInstanceOf(ServiceException::class.java)
                .hasMessageContaining("호텔이 존재하지 않습니다")
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 실패 - 이미 추가된 호텔")
    fun addFavorite_Fail_AlreadyFavorite() {
        // 먼저 즐겨찾기에 추가
        memberService.addFavorite(testHotel.id)
        
        // when & then
        assertThatThrownBy { memberService.addFavorite(testHotel.id) }
                .isInstanceOf(ServiceException::class.java)
                .hasMessageContaining("이미 즐겨찾기에 추가된 호텔입니다")
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 삭제 성공")
    fun removeFavorite_Success() {
        // given
        memberService.addFavorite(testHotel.id)
        
        // when
        memberService.removeFavorite(testHotel.id)
        
        // then
        val updatedMember = memberRepository.findByMemberEmail("test@example.com").orElseThrow()
        assertThat(updatedMember.favoriteHotels).doesNotContain(testHotel)
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 목록 조회 성공")
    fun getFavoriteHotels_Success() {
        // given
        memberService.addFavorite(testHotel.id)
        
        // when
        val favorites = memberService.getFavoriteHotels()
        
        // then
        assertThat(favorites).hasSize(1)
        assertThat(favorites[0].hotelId).isEqualTo(testHotel.id)
        assertThat(favorites[0].hotelName).isEqualTo(testHotel.hotelName)
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("특정 호텔이 즐겨찾기인지 확인 성공")
    fun isFavoriteHotel_Success() {
        // given
        memberService.addFavorite(testHotel.id)
        
        // when
        val result = memberService.isFavoriteHotel(testHotel.id)
        
        // then
        assertThat(result).isTrue()
    }
} 
package com.ll.hotel.domain.member.member.controller

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.request.Rq
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FavoriteControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var hotelRepository: HotelRepository

    @Mock
    private lateinit var rq: Rq

    private lateinit var testMember: Member
    private lateinit var testHotel1: Hotel
    private lateinit var testHotel2: Hotel

    @BeforeEach
    fun setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this)
        
        // MemberService에 모의 Rq 주입
        ReflectionTestUtils.setField(memberService, "rq", rq)
        
        // 테스트 데이터 초기화
        testMember = Member.builder()
                .memberEmail("test@example.com")
                .memberName("테스트회원")
                .memberPhoneNumber("010-1234-5678")
                .role(Role.USER)
                .memberStatus(MemberStatus.ACTIVE)
                .favoriteHotels(HashSet())
                .build()
        
        memberRepository.save(testMember)
        
        // Rq 모킹 설정
        Mockito.`when`(rq.actor).thenReturn(testMember)
        
        // 테스트 호텔 설정
        testHotel1 = Hotel.builder()
                .hotelName("테스트 호텔1")
                .streetAddress("서울시 강남구")
                .zipCode(12345)
                .hotelGrade(5)
                .checkInTime(LocalTime.of(14, 0))
                .checkOutTime(LocalTime.of(12, 0))
                .hotelExplainContent("호텔 설명")
                .hotelStatus(HotelStatus.PENDING)
                .averageRating(4.5)
                .totalReviewRatingSum(0L)
                .totalReviewCount(0L)
                .hotelEmail("hotel1@example.com")
                .hotelPhoneNumber("010-1111-1111")
                .favorites(HashSet())
                .build()
        
        testHotel2 = Hotel.builder()
                .hotelName("테스트 호텔2")
                .streetAddress("서울시 서초구")
                .zipCode(54321)
                .hotelGrade(4)
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .hotelExplainContent("호텔 설명2")
                .hotelStatus(HotelStatus.PENDING)
                .averageRating(4.0)
                .totalReviewRatingSum(0L)
                .totalReviewCount(0L)
                .hotelEmail("hotel2@example.com")
                .hotelPhoneNumber("010-2222-2222")
                .favorites(HashSet())
                .build()
        
        hotelRepository.save(testHotel1)
        hotelRepository.save(testHotel2)
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 - 성공")
    fun addFavorite_Success() {
        // when & then
        mockMvc.perform(post("/api/favorites/" + testHotel1.id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent)
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 삭제 성공")
    fun removeFavorite_Success() {
        // 먼저 즐겨찾기 추가
        memberService.addFavorite(testHotel1.id)
        
        // 즐겨찾기 삭제 요청
        mockMvc.perform(delete("/api/favorites/" + testHotel1.id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent)
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 목록 조회 성공")
    fun getFavorites_Success() {
        // 호텔을 즐겨찾기에 추가
        memberService.addFavorite(testHotel1.id)
        memberService.addFavorite(testHotel2.id)
        
        mockMvc.perform(get("/api/favorites/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.resultCode").value("OK"))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(2))
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 목록이 없을 때")
    fun getFavorites_Empty() {
        mockMvc.perform(get("/api/favorites/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.resultCode").value("OK"))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(0))
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("특정 호텔 즐겨찾기 여부 확인")
    fun checkFavorite_Success() {
        // given
        memberService.addFavorite(testHotel1.id)

        // when & then
        mockMvc.perform(get("/api/favorites/me/" + testHotel1.id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.resultCode").value("OK"))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").value(true))
    }
} 
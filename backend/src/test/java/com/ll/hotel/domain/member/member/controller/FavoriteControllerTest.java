package com.ll.hotel.domain.member.member.controller;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.domain.member.member.service.MemberService;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import com.ll.hotel.global.request.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Mock
    private Rq rq;

    private Member testMember;
    private Hotel testHotel1;
    private Hotel testHotel2;

    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);
        
        // MemberService에 모의 Rq 주입
        ReflectionTestUtils.setField(memberService, "rq", rq);
        
        // 테스트 데이터 초기화
        testMember = Member.builder()
                .memberEmail("test@example.com")
                .memberName("테스트회원")
                .memberPhoneNumber("010-1234-5678")
                .role(Role.USER)
                .memberStatus(MemberStatus.ACTIVE)
                .favoriteHotels(new HashSet<>())
                .build();
        
        memberRepository.save(testMember);
        
        // Rq 모킹 설정
        Mockito.when(rq.getActor()).thenReturn(testMember);
        
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
                .favorites(new HashSet<>())
                .build();
        
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
                .favorites(new HashSet<>())
                .build();
        
        hotelRepository.save(testHotel1);
        hotelRepository.save(testHotel2);
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 - 성공")
    void addFavorite_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/favorites/" + testHotel1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 삭제 성공")
    void removeFavorite_Success() throws Exception {
        // 먼저 즐겨찾기 추가
        memberService.addFavorite(testHotel1.getId());
        
        // 즐겨찾기 삭제 요청
        mockMvc.perform(delete("/api/favorites/" + testHotel1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 목록 조회 성공")
    void getFavorites_Success() throws Exception {
        // 호텔을 즐겨찾기에 추가
        memberService.addFavorite(testHotel1.getId());
        memberService.addFavorite(testHotel2.getId());
        
        mockMvc.perform(get("/api/favorites/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("OK"))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 목록이 없을 때")
    void getFavorites_Empty() throws Exception {
        mockMvc.perform(get("/api/favorites/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("OK"))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("특정 호텔 즐겨찾기 여부 확인")
    void checkFavorite_Success() throws Exception {
        // given
        memberService.addFavorite(testHotel1.getId());

        // when & then
        mockMvc.perform(get("/api/favorites/me/" + testHotel1.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("OK"))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").value(true));
    }
} 
package com.ll.hotel.domain.member.member.service;

import com.ll.hotel.domain.member.member.dto.FavoriteDto;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import com.ll.hotel.global.exceptions.ServiceException;
import com.ll.hotel.global.request.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceFavoriteTest {

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Mock
    private Rq rq;
    
    private Hotel testHotel;
    private Member testMember;
    
    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);
        
        // MemberService에 모의 Rq 주입
        ReflectionTestUtils.setField(memberService, "rq", rq);
        
        // 테스트 데이터 초기화
        testMember = Member.builder()
                .memberEmail("test@example.com")
                .memberName("Test User")
                .memberPhoneNumber("010-1234-5678")
                .role(Role.USER)
                .memberStatus(MemberStatus.ACTIVE)
                .favoriteHotels(new HashSet<>())
                .build();
        
        memberRepository.save(testMember);
        
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
                .favorites(new HashSet<>())
                .build();
        
        hotelRepository.save(testHotel);
        
        // Rq 모킹 설정
        Mockito.when(rq.getActor()).thenReturn(testMember);
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 - 성공")
    void addFavorite_Success() {
        // when
        memberService.addFavorite(testHotel.getId());
        
        // then
        Member updatedMember = memberRepository.findByMemberEmail("test@example.com").orElseThrow();
        assertThat(updatedMember.getFavoriteHotels()).contains(testHotel);
    }
    
    @Test
    @DisplayName("즐겨찾기 추가 실패 - 로그인하지 않은 경우")
    void addFavorite_Fail_NotLoggedIn() {
        // 로그인하지 않은 상태 모킹
        Mockito.when(rq.getActor()).thenReturn(null);
        
        // when & then
        assertThatThrownBy(() -> memberService.addFavorite(testHotel.getId()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("로그인이 필요합니다");
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 실패 - 존재하지 않는 호텔")
    void addFavorite_Fail_HotelNotFound() {
        // when & then
        assertThatThrownBy(() -> memberService.addFavorite(9999L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("호텔이 존재하지 않습니다");
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 추가 실패 - 이미 추가된 호텔")
    void addFavorite_Fail_AlreadyFavorite() {
        // 먼저 즐겨찾기에 추가
        memberService.addFavorite(testHotel.getId());
        
        // when & then
        assertThatThrownBy(() -> memberService.addFavorite(testHotel.getId()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("이미 즐겨찾기에 추가된 호텔입니다");
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 삭제 성공")
    void removeFavorite_Success() {
        // given
        memberService.addFavorite(testHotel.getId());
        
        // when
        memberService.removeFavorite(testHotel.getId());
        
        // then
        Member updatedMember = memberRepository.findByMemberEmail("test@example.com").orElseThrow();
        assertThat(updatedMember.getFavoriteHotels()).doesNotContain(testHotel);
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("즐겨찾기 목록 조회 성공")
    void getFavoriteHotels_Success() {
        // given
        memberService.addFavorite(testHotel.getId());
        
        // when
        List<FavoriteDto> favorites = memberService.getFavoriteHotels();
        
        // then
        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).hotelId()).isEqualTo(testHotel.getId());
        assertThat(favorites.get(0).hotelName()).isEqualTo(testHotel.getHotelName());
    }
    
    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("특정 호텔이 즐겨찾기인지 확인 성공")
    void isFavoriteHotel_Success() {
        // given
        memberService.addFavorite(testHotel.getId());
        
        // when
        boolean result = memberService.isFavoriteHotel(testHotel.getId());
        
        // then
        assertThat(result).isTrue();
    }
} 
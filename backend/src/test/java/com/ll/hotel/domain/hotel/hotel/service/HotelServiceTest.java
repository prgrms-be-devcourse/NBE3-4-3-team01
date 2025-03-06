package com.ll.hotel.domain.hotel.hotel.service;

import com.ll.hotel.domain.hotel.hotel.dto.*;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.option.service.HotelOptionService;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.hotel.room.repository.RoomRepository;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.repository.BusinessRepository;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import com.ll.hotel.global.exceptions.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HotelServiceTest {
    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelOptionService hotelOptionService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("호텔 생성")
    public void createHotel() {
        // given
        Business business = this.createBusiness("새사장1", "newBusiness1@gmail.com");
        Member actor = business.getMember();
        Set<String> hotelOptions = this.hotelOptionService.findAll()
                .stream()
                .map(HotelOption::getName)
                .collect(Collectors.toSet());

        PostHotelRequest postHotelRequest = new PostHotelRequest("호텔3", "hotel3@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔3입니다.", null, hotelOptions);

        // when
        PostHotelResponse postHotelResponse = this.hotelService.createHotel(actor, postHotelRequest);

        Hotel hotel = this.hotelRepository.findById(postHotelResponse.hotelId()).get();

        business.setHotel(hotel);
        this.businessRepository.save(business);

        // then
        assertEquals(this.hotelRepository.count(), 2L);
        assertEquals(hotel.getHotelName(), "호텔3");
        assertEquals(hotel.getHotelEmail(), "hotel3@naver.com");
        assertEquals(hotel.getHotelPhoneNumber(), "010-1234-1234");
        assertEquals(hotel.getBusiness().getId(), business.getId());
        assertEquals(hotel.getBusiness().getMember().getRole(), Role.BUSINESS);
        assertEquals(hotel.getBusiness().getHotel(), hotel);
        assertEquals(hotel.getHotelOptions().size(), 2);

        Set<String> hotelNames = hotel.getHotelOptions().stream()
                .map(HotelOption::getName)
                .collect(Collectors.toSet());

        assertTrue(hotelNames.contains("무료 Wi-Fi"));
        assertTrue(hotelNames.contains("프론트 데스크"));
    }

    @Test
    @DisplayName("호텔 생성 실패 - 비사업가 호텔 생성 시도")
    public void createHotelFailed_notBusiness() {
        // given
        Member actor = this.memberRepository.findByMemberName("customer1").get();

        Set<String> hotelOptions = new HashSet<>();

        PostHotelRequest postHotelRequest = new PostHotelRequest("호텔1", "hotel@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", null, hotelOptions);

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.createHotel(actor, postHotelRequest);
        });

        // then
        assertEquals(403, exception.getResultCode().value());
        assertEquals("사업자만 관리할 수 있습니다.", exception.getMsg());
    }

    @Test
    @DisplayName("호텔 생성 실패 - 사업가 1개 이상 호텔 생성 시도")
    public void createHotelFailed_severalCreate() {
        // given
        Member actor = this.memberRepository.findByMemberName("business1").get();

        Set<String> hotelOptions = new HashSet<>();

        PostHotelRequest postHotelRequest = new PostHotelRequest("호텔1", "hotel@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", null, hotelOptions);

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.createHotel(actor, postHotelRequest);
        });

        // then
        assertEquals(409, exception.getResultCode().value());
        assertEquals("한 사업자는 하나의 호텔만 등록할 수 있습니다.", exception.getMsg());
    }

    @Test
    @DisplayName("호텔 생성 실패 - 존재하지 않는 호텔 옵션")
    public void createHotel_invalidHotelOptions() {
        // given
        Business business = this.createBusiness("새사장1", "newBusiness1@gmail.com");
        Member actor = business.getMember();

        Set<String> hotelOptions = new HashSet<>(Set.of("Parking_lot", "Breakfast", "Lunch"));

        PostHotelRequest postHotelRequest = new PostHotelRequest("호텔1", "hotel@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", null, hotelOptions);

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.createHotel(actor, postHotelRequest);
        });

        // then
        assertEquals(404, exception.getResultCode().value());
        assertEquals("사용할 수 없는 호텔 옵션이 존재합니다.", exception.getMsg());
    }

    @Test
    @DisplayName("호텔 전체 목록 조회")
    public void findAllHotels() {
        // given_1
        Business business = this.createBusiness("새사장1", "newHotel1@gmail.com");

        PostHotelRequest req1 = new PostHotelRequest("호텔1", "hotel@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", null, null);

        PostHotelResponse hotelRes = this.hotelService.createHotel(business.getMember(), req1);
        Hotel hotel = this.hotelRepository.findById(hotelRes.hotelId()).get();

        Room room = Room.builder()
                .roomName("새객실1")
                .roomNumber(3)
                .hotel(hotel)
                .standardNumber(2)
                .maxNumber(6)
                .build();

        this.roomRepository.save(room);
        List<Room> rooms = hotel.getRooms();
        rooms.add(room);
        hotel.setRooms(rooms);
        this.hotelRepository.save(hotel);

        // given_2
        business = this.createBusiness("새사장2", "newHotel2@gmail.com");

        PostHotelRequest req2 = new PostHotelRequest("호텔2", "sin@naver.com",
                "010-1111-1111", "부산시", 1111,
                5, LocalTime.of(14, 0), LocalTime.of(16, 0), "신호텔", null, null);

        hotelRes = this.hotelService.createHotel(business.getMember(), req2);
        hotel = this.hotelRepository.findById(hotelRes.hotelId()).get();

        room = Room.builder()
                .roomName("새객실2")
                .roomNumber(3)
                .hotel(hotel)
                .standardNumber(2)
                .maxNumber(6)
                .build();

        this.roomRepository.save(room);
        rooms = hotel.getRooms();
        rooms.add(room);
        hotel.setRooms(rooms);
        this.hotelRepository.save(hotel);

        // tempLine
        Page<HotelWithImageDto> allHotels = this.hotelRepository.findAllHotels(ImageType.HOTEL, "서울",
                PageRequest.of(1, 10));
        List<Hotel> hotels = this.hotelRepository.findAll();
        //

        // when
        Page<GetHotelResponse> resultPage = this.hotelService.findAllHotels(1, 10, "latest", "asc", "",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(31), 2);
        List<GetHotelResponse> list = resultPage.getContent();
        GetHotelResponse resFirst = list.getFirst();
        GetHotelResponse resLast = list.getLast();

        // then
        assertEquals(this.hotelRepository.findAll().size(), list.size());
        assertEquals(resFirst.hotelName(), "강남호텔");
        assertEquals(resLast.hotelName(), "호텔2");
        assertEquals(resFirst.streetAddress(), "서울시 강남구 호텔로 10");
        assertEquals(resLast.streetAddress(), "부산시");
    }

    @Test
    @DisplayName("호텔 전체 목록 조회 - filterDirection 값을 입력하지 않았을 경우")
    public void findAllHotelsWithoutFilterDirection() {
        // given_1
        Business business = this.createBusiness("새사장1", "newHotel1@gmail.com");

        PostHotelRequest req1 = new PostHotelRequest("호텔1", "hotel@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", null, null);

        PostHotelResponse hotelRes = this.hotelService.createHotel(business.getMember(), req1);
        Hotel hotel = this.hotelRepository.findById(hotelRes.hotelId()).get();

        Room room = Room.builder()
                .roomName("새객실1")
                .roomNumber(3)
                .hotel(hotel)
                .standardNumber(2)
                .maxNumber(6)
                .build();

        this.roomRepository.save(room);
        List<Room> rooms = hotel.getRooms();
        rooms.add(room);
        hotel.setRooms(rooms);
        this.hotelRepository.save(hotel);

        // given_2
        business = this.createBusiness("새사장2", "newHotel2@gmail.com");

        PostHotelRequest req2 = new PostHotelRequest("호텔2", "sin@naver.com",
                "010-1111-1111", "부산시", 1111,
                5, LocalTime.of(14, 0), LocalTime.of(16, 0), "신호텔", null, null);

        hotelRes = this.hotelService.createHotel(business.getMember(), req2);
        hotel = this.hotelRepository.findById(hotelRes.hotelId()).get();

        room = Room.builder()
                .roomName("새객실2")
                .roomNumber(3)
                .hotel(hotel)
                .standardNumber(2)
                .maxNumber(6)
                .build();

        this.roomRepository.save(room);
        rooms = hotel.getRooms();
        rooms.add(room);
        hotel.setRooms(rooms);
        this.hotelRepository.save(hotel);

        // when
        Page<GetHotelResponse> resultPage = this.hotelService.findAllHotels(1, 10, "latest", null, "",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(31), 2);
        List<GetHotelResponse> list = resultPage.getContent();
        GetHotelResponse resFirst = list.getFirst();
        GetHotelResponse resLast = list.getLast();

        // then
        assertEquals(this.hotelRepository.findAll().size(), list.size());
        assertEquals(resFirst.hotelName(), "호텔2");
        assertEquals(resLast.hotelName(), "강남호텔");
        assertEquals(resFirst.streetAddress(), "부산시");
        assertEquals(resLast.streetAddress(), "서울시 강남구 호텔로 10");
    }

    @Test
    @DisplayName("호텔 전체 목록 조회 - 주소지 검색")
    public void findAllHotelsWithStreetAddress() {
        // given_1
        Business business = this.createBusiness("새사장1", "newHotel1@gmail.com");

        PostHotelRequest req1 = new PostHotelRequest("호텔1", "hotel@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", null, null);

        PostHotelResponse hotelRes = this.hotelService.createHotel(business.getMember(), req1);
        Hotel hotel = this.hotelRepository.findById(hotelRes.hotelId()).get();

        Room room = Room.builder()
                .roomName("새객실1")
                .roomNumber(3)
                .hotel(hotel)
                .standardNumber(2)
                .maxNumber(6)
                .build();

        this.roomRepository.save(room);
        List<Room> rooms = hotel.getRooms();
        rooms.add(room);
        hotel.setRooms(rooms);
        this.hotelRepository.save(hotel);

        // given_2
        business = this.createBusiness("새사장2", "newHotel2@gmail.com");

        PostHotelRequest req2 = new PostHotelRequest("호텔2", "sin@naver.com",
                "010-1111-1111", "부산시", 1111,
                5, LocalTime.of(14, 0), LocalTime.of(16, 0), "신호텔", null, null);

        hotelRes = this.hotelService.createHotel(business.getMember(), req2);
        hotel = this.hotelRepository.findById(hotelRes.hotelId()).get();

        room = Room.builder()
                .roomName("새객실2")
                .roomNumber(3)
                .hotel(hotel)
                .standardNumber(2)
                .maxNumber(6)
                .build();

        this.roomRepository.save(room);
        rooms = hotel.getRooms();
        rooms.add(room);
        hotel.setRooms(rooms);
        this.hotelRepository.save(hotel);

        // when
        Page<GetHotelResponse> resultPage = this.hotelService.findAllHotels(1, 10, "latest", "asc", "서울",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(31), 2);
        List<GetHotelResponse> list = resultPage.getContent();
        GetHotelResponse resFirst = list.getFirst();
        GetHotelResponse resLast = list.getLast();

        // then
        assertEquals(2, list.size());
        assertEquals(resFirst.hotelName(), "강남호텔");
        assertEquals(resLast.hotelName(), "호텔1");
        assertEquals(resFirst.streetAddress(), "서울시 강남구 호텔로 10");
        assertEquals(resLast.streetAddress(), "서울시");
    }

    @Test
    @DisplayName("호텔 단일 목록 조회")
    public void findHotelDetail() {
        // given_1
        Member actor = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(actor).get();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        // when_1
        GetHotelDetailResponse detRes = this.hotelService.findHotelDetail(hotel.getId());

        // then_1
        assertEquals(hotel.getId(), detRes.hotelDetailDto().hotelId());
        assertEquals("강남호텔", detRes.hotelDetailDto().hotelName());
        assertEquals("서울시 강남구 호텔로 10", detRes.hotelDetailDto().streetAddress());
        assertTrue(detRes.hotelDetailDto().hotelOptions().contains("무료 Wi-Fi"));
        assertTrue(detRes.hotelDetailDto().hotelOptions().contains("프론트 데스크"));

        // given_2
        business = this.createBusiness("새사장1", "newHotel1@gmail.com");

        PostHotelRequest req1 = new PostHotelRequest("호텔1", "hotel@naver.com",
                "010-1234-1234", "서울시", 0123,
                3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", null, null);

        PostHotelResponse res1 = this.hotelService.createHotel(business.getMember(), req1);
        hotel = this.hotelRepository.findById(res1.hotelId()).get();

        business.setHotel(hotel);
        this.businessRepository.save(business);

        // when_2
        detRes = this.hotelService.findHotelDetail(hotel.getId());

        // then_2
        assertEquals(res1.hotelId(), detRes.hotelDetailDto().hotelId());
        assertEquals("호텔1", detRes.hotelDetailDto().hotelName());
        assertEquals("서울시", detRes.hotelDetailDto().streetAddress());
        assertEquals(detRes.hotelDetailDto().hotelOptions().size(), 0);
    }

    @Test
    @DisplayName("호텔 수정")
    public void modifyHotel() {
        // given
        Member actor = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(actor).get();
        Set<String> hotelOptions = new HashSet<>(Set.of("무료 Wi-Fi"));

        Hotel hotel = this.hotelRepository.findByBusiness(business).get();
        long hotelId = hotel.getId();

        PutHotelRequest req1 = new PutHotelRequest("수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 0123, 1,
                LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name(), null, null, hotelOptions);

        // when
        PutHotelResponse res1 = this.hotelService.modifyHotel(hotelId, actor, req1);

        hotel = this.hotelRepository.findById(hotelId).get();

        Set<String> hotelOptionNames = hotel.getHotelOptions().stream()
                .map(HotelOption::getName)
                .collect(Collectors.toSet());

        // then
        assertEquals(hotel.getId(), res1.hotelId());
        assertEquals(hotel.getStreetAddress(), req1.streetAddress());
        assertEquals(hotel.getZipCode(), req1.zipCode());
        assertEquals(hotel.getCheckInTime(), req1.checkInTime());
        assertEquals(hotel.getHotelName(), res1.hotelName());
        assertEquals(hotel.getHotelEmail(), req1.hotelEmail());
        assertEquals(1, hotelOptionNames.size());
        assertTrue(hotelOptionNames.contains("무료 Wi-Fi"));
        assertFalse(hotelOptionNames.contains("프론트 데스크"));
    }

    @Test
    @DisplayName("호텔 수정 실패 - 사업자가 아닐 경우")
    public void modifyHotelFailed_notBusiness() {
        // given
        Member actor = this.memberRepository.findByMemberName("customer1").get();

        Member businessMem = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(businessMem).get();
        Set<String> hotelOptions = new HashSet<>();

        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        PutHotelRequest req1 = new PutHotelRequest("수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 0123, 1,
                LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name(), null, null, hotelOptions);

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.modifyHotel(hotel.getId(), actor, req1);
        });

        // then
        assertEquals(403, exception.getResultCode().value());
        assertEquals("사업자만 관리할 수 있습니다.", exception.getMsg());
    }

    @Test
    @DisplayName("호텔 수정 실패 - 호텔 소유주가 아닐 경우")
    public void modifyHotelFailed_notEqualBusiness() {
        // given
        Business newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com");

        Member businessMem = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(businessMem).get();
        Set<String> hotelOptions = new HashSet<>();

        Hotel hotel = this.hotelRepository.findByBusiness(business).get();
        long hotelId = hotel.getId();

        PutHotelRequest req1 = new PutHotelRequest("수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 0123, 1,
                LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name(), null, null, hotelOptions);

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.modifyHotel(hotelId, newBusiness.getMember(), req1);
        });

        // then
        assertEquals(403, exception.getResultCode().value());
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.getMsg());
    }

    @Test
    @DisplayName("호텔 수정 실패 - 존재하지 않는 호텔 옵션")
    public void modifyHotelFailed_invalidHotelOptions() {
        // given
        Member actor = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(actor).get();
        Set<String> hotelOptions = new HashSet<>(Set.of("에어컨"));

        Hotel hotel = this.hotelRepository.findByBusiness(business).get();
        long hotelId = hotel.getId();

        PutHotelRequest req1 = new PutHotelRequest("수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 0123, 1,
                LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name(), null, null, hotelOptions);

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.modifyHotel(hotelId, actor, req1);
        });

        // then
        assertEquals(404, exception.getResultCode().value());
        assertEquals("사용할 수 없는 호텔 옵션이 존재합니다.", exception.getMsg());
    }

    @Test
    @DisplayName("호텔 삭제")
    public void deleteHotel() {
        // given
        Member actor = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(actor).get();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        // when
        this.hotelService.deleteHotel(hotel.getId(), actor);

        // then
        assertEquals(HotelStatus.UNAVAILABLE, hotel.getHotelStatus());
    }

    @Test
    @DisplayName("호텔 삭제 실패 - 사업자가 아닐 경우")
    public void deleteHotelFailed_notBusiness() {
        // given
        Member actor = this.memberRepository.findByMemberName("customer1").get();

        Member businessMem = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(businessMem).get();

        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.deleteHotel(hotel.getId(), actor);
        });

        // then
        assertEquals(403, exception.getResultCode().value());
        assertEquals("사업자만 관리할 수 있습니다.", exception.getMsg());
    }

    @Test
    @DisplayName("호텔 삭제 실패 - 호텔 소유주가 아닐 경우")
    public void deleteHotelFailed_notEqualBusiness() {
        // given
        Business newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com");

        Member actor = this.memberRepository.findByMemberName("business1").get();
        Business business = this.businessRepository.findByMember(actor).get();

        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        // when
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.hotelService.deleteHotel(hotel.getId(), newBusiness.getMember());
        });

        // then
        assertEquals(403, exception.getResultCode().value());
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.getMsg());
    }

    // 사업가 생성
    private Business createBusiness(String name, String email) {
        // 회원 생성
        Member member = Member
                .builder()
                .birthDate(LocalDate.now())
                .memberEmail(email)
                .memberName(name)
                .memberPhoneNumber("01011111111")
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.BUSINESS)
                .build();

        // 회원 저장
        memberRepository.save(member);

        // 사업가 등록
        Business business = Business
                .builder()
                .businessRegistrationNumber(createRegistrationNumber())
                .startDate(LocalDate.now())
                .ownerName(name)
                .approvalStatus(BusinessApprovalStatus.APPROVED)
                .member(member)
                .hotel(null)
                .build();

        // 사업가 저장
        businessRepository.save(business);

        return business;
    }

    // 사업가 번호 난수 생성
    private String createRegistrationNumber() {
        // 1000000000 ~ 9999999999 난수 생성
        return String.valueOf((long) ((Math.random() * 9000000000L) + 1000000000L));
    }
}
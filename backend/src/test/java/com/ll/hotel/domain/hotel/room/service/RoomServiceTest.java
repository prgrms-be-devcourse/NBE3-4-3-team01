package com.ll.hotel.domain.hotel.room.service;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.hotel.service.HotelService;
import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import com.ll.hotel.domain.hotel.option.service.RoomOptionService;
import com.ll.hotel.domain.hotel.room.dto.*;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.hotel.room.repository.RoomRepository;
import com.ll.hotel.domain.hotel.room.type.BedTypeNumber;
import com.ll.hotel.domain.hotel.room.type.RoomStatus;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoomServiceTest {
    @Autowired
    private HotelService hotelService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomOptionService roomOptionService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Test
    @DisplayName("객실 생성")
    public void createRoom() {
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Set<String> roomOptions = new HashSet<>(Set.of("객실 내 금고", "미니 냉장고"));
        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();
        Room room = this.roomRepository.findById(roomId).get();

        Set<String> roomOption = room.getRoomOptions().stream().map(RoomOption::getName).collect(Collectors.toSet());

        assertEquals(room.getRoomName(), "객실1");
        assertEquals(hotel.getId(), res1.hotelId());
        assertEquals(room.getBasePrice(), 300000);
        assertEquals(room.getBedTypeNumber().bed_single(), 4);
        assertEquals(room.getBedTypeNumber().bed_double(), 2);
        assertEquals(room.getBedTypeNumber().bed_king(), 1);
        assertEquals(room.getBedTypeNumber().bed_triple(), 0);
        assertEquals(room.getStandardNumber(), 2);
        assertEquals(roomOption.size(), 2);
        assertTrue(roomOption.contains("객실 내 금고"));
        assertTrue(roomOption.contains("미니 냉장고"));
    }

    @Test
    @DisplayName("객실 생성 실패 - 존재하지 않는 객실 옵션")
    public void createRoomFailed_invalidRoomOptions() {
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Set<String> roomOptions = new HashSet<>(Set.of("청소기"));
        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.createRoom(hotel.getId(), business.getMember(), req1);
        });

        assertEquals(404, exception.getResultCode().value());
        assertEquals("사용할 수 없는 객실 옵션이 존재합니다.", exception.getMsg());
    }

    @Test
    @DisplayName("객실 생성 실패 - 사업가가 아닐 경우")
    public void createRoomFailed_notBusiness() {
        Member actor = this.getActor();
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Set<String> roomOptions = new HashSet<>(Set.of("객실 내 금고", "미니 냉장고"));
        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.createRoom(hotel.getId(), actor, req1);
        });

        assertEquals(403, exception.getResultCode().value());
        assertEquals("사업자만 관리할 수 있습니다.", exception.getMsg());
    }

    @Test
    @DisplayName("객실 생성 실패 - 호텔 소유주가 아닐 경우")
    public void createRoomFailed_notEqualBusiness() {
        Business newBusiness = this.createBusiness("새사장1","newHotel1@gmail.com");
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Set<String> roomOptions = new HashSet<>(Set.of("객실 내 금고", "미니 냉장고"));
        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.createRoom(hotel.getId(), newBusiness.getMember(), req1);
        });

        assertEquals(403, exception.getResultCode().value());
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.getMsg());
    }

    @Test
    @DisplayName("객실 전체 조회")
    public void findAllRooms() {
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Set<String> roomOptions = new HashSet<>(Set.of("객실 내 금고", "미니 냉장고"));
        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();

        bedTypeNumber = Map.of("DOUBLE", 4, "QUEEN", 1);
        PostRoomRequest req2 = new PostRoomRequest("객실2", 2, 500000, 3, 4, bedTypeNumber, null, null);

        PostRoomResponse res2 = this.roomService.createRoom(hotel.getId(), business.getMember(), req2);

        List<GetRoomResponse> rooms = this.roomService.findAllRooms(hotel.getId());
        GetRoomResponse res = rooms.get(1);

        assertEquals(rooms.size(), 3);
        assertEquals(res.roomId(), roomId);
        assertEquals(res.roomName(), "객실1");
        assertEquals(res.basePrice(), 300000);
        assertEquals(res.bedTypeNumber().bed_single(), 4);
        assertEquals(res.bedTypeNumber().bed_double(), 2);
        assertEquals(res.bedTypeNumber().bed_king(), 1);
        assertEquals(res.bedTypeNumber().bed_triple(), 0);
        assertEquals(res.standardNumber(), 2);

        res = rooms.get(2);

        assertEquals(res2.roomId(), roomId + 1);
        assertEquals(res.roomName(), "객실2");
        assertEquals(res.basePrice(), 500000);
        assertEquals(res.bedTypeNumber().bed_double(), 4);
        assertEquals(res.bedTypeNumber().bed_queen(), 1);
        assertEquals(res.bedTypeNumber().bed_triple(), 0);
        assertEquals(res.standardNumber(), 3);
    }

    @Test
    @DisplayName("특정 객실 조회")
    public void findRoom() {
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Set<String> roomOptions = new HashSet<>(Set.of("객실 내 금고", "미니 냉장고"));
        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();

        bedTypeNumber = Map.of("DOUBLE", 4, "QUEEN", 1);
        PostRoomRequest req2 = new PostRoomRequest("객실2", 2, 500000, 3, 4, bedTypeNumber, null, null);

        PostRoomResponse res2 = this.roomService.createRoom(hotel.getId(), business.getMember(), req2);

        GetRoomDetailResponse detRes1 = this.roomService.findRoomDetail(hotel.getId(), roomId);

        assertEquals(detRes1.roomDto().id(), roomId);
        assertEquals(detRes1.roomDto().hotelId(), hotel.getId());
        assertEquals(detRes1.roomDto().roomName(), "객실1");
        assertEquals(detRes1.roomDto().roomNumber(), req1.roomNumber());
        assertEquals(detRes1.roomDto().basePrice(), req1.basePrice());
        assertEquals(detRes1.roomDto().bedTypeNumber().bed_single(), req1.bedTypeNumber().get("SINGLE"));
        assertEquals(detRes1.roomDto().bedTypeNumber().bed_double(), req1.bedTypeNumber().get("DOUBLE"));
        assertEquals(detRes1.roomDto().bedTypeNumber().bed_king(), req1.bedTypeNumber().get("KING"));
        assertEquals(detRes1.roomDto().bedTypeNumber().bed_triple(), 0);
        assertEquals(detRes1.roomDto().roomStatus(), RoomStatus.AVAILABLE.name());
        assertEquals(detRes1.roomImageUrls().size(), 0);
        assertEquals(detRes1.roomDto().roomOptions().size(), 2);
        assertEquals(detRes1.roomDto().standardNumber(), 2);

        roomId = res2.roomId();
        detRes1 = this.roomService.findRoomDetail(hotel.getId(), roomId);

        assertEquals(detRes1.roomDto().id(), roomId);
        assertEquals(detRes1.roomDto().hotelId(), hotel.getId());
        assertEquals(detRes1.roomDto().roomName(), "객실2");
        assertEquals(detRes1.roomDto().roomNumber(), req2.roomNumber());
        assertEquals(detRes1.roomDto().basePrice(), req2.basePrice());
        assertEquals(detRes1.roomDto().bedTypeNumber().bed_double(), req2.bedTypeNumber().get("DOUBLE"));
        assertEquals(detRes1.roomDto().bedTypeNumber().bed_queen(), req2.bedTypeNumber().get("QUEEN"));
        assertEquals(detRes1.roomDto().bedTypeNumber().bed_triple(), 0);
        assertEquals(detRes1.roomDto().roomStatus(), RoomStatus.AVAILABLE.name());
        assertEquals(detRes1.roomImageUrls().size(), 0);
        assertEquals(detRes1.roomDto().roomOptions().size(), 0);
        assertEquals(detRes1.roomDto().standardNumber(), 3);
    }

    @Test
    @DisplayName("객실 수정")
    public void modifyRoom() {
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);
        Set<String> roomOptions = this.roomOptionService.findAll()
                .stream()
                .map(RoomOption::getName)
                .collect(Collectors.toSet());

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();
        Room room = this.roomRepository.findById(roomId).get();

        roomOptions = new HashSet<>(Set.of("미니 냉장고"));
        PutRoomRequest putReq1 = new PutRoomRequest("수정 객실1", 5, null, null, 5, null,
                "in_booking", null, null, roomOptions);

        PutRoomResponse putRes1 = this.roomService.modifyRoom(hotel.getId(), roomId, business.getMember(), putReq1);

        assertEquals(putRes1.hotelId(), hotel.getId());
        assertEquals(putRes1.roomId(), room.getId());
        assertEquals(putRes1.roomName(), room.getRoomName());
        assertEquals(putRes1.roomStatus(), RoomStatus.IN_BOOKING.getValue());

        room = this.roomRepository.findById(roomId).get();

        Set<String> roomNames = room.getRoomOptions().stream()
                .map(RoomOption::getName)
                .collect(Collectors.toSet());

        assertEquals(room.getRoomName(), putReq1.roomName());
        assertEquals(room.getRoomNumber(), putReq1.roomNumber());
        assertEquals(room.getBasePrice(), req1.basePrice());
        assertEquals(room.getStandardNumber(), req1.standardNumber());
        assertEquals(room.getMaxNumber(), putReq1.maxNumber());
        assertEquals(room.getBedTypeNumber(), BedTypeNumber.fromJson(req1.bedTypeNumber()));
        assertEquals(room.getRoomStatus(), RoomStatus.IN_BOOKING);
        assertEquals(room.getHotel().getId(), hotel.getId());
        assertEquals(1, roomOptions.size());
        assertEquals(roomNames, roomOptions);
    }

    @Test
    @DisplayName("객실 수정 실패 - 존재하지 않는 객실 옵션")
    public void modifyRoomFailed_invalidRoomOption() {
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, new HashSet<>());

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();

        Set<String> roomOptions = new HashSet<>(Set.of("TV", "AirConditioner"));
        PutRoomRequest putReq1 = new PutRoomRequest("수정 객실1", 5, null, null, 5, null,
                "in_booking", null, null, roomOptions);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.modifyRoom(hotel.getId(), roomId, business.getMember(), putReq1);
        });

        assertEquals(404, exception.getResultCode().value());
        assertEquals("사용할 수 없는 객실 옵션이 존재합니다.", exception.getMsg());
    }

    @Test
    @DisplayName("객실 수정 실패 - 사업자가 아닐 경우")
    public void modifyRoomFailed_notBusiness() {
        Member actor = this.getActor();
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);
        Set<String> roomOptions = this.roomOptionService.findAll()
                .stream()
                .map(RoomOption::getName)
                .collect(Collectors.toSet());

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();

        roomOptions = new HashSet<>(Set.of("미니 냉장고"));
        PutRoomRequest putReq1 = new PutRoomRequest("수정 객실1", 5, null, null, 5, null,
                "in_booking", null, null, roomOptions);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.modifyRoom(hotel.getId(), roomId, actor, putReq1);
        });

        assertEquals(403, exception.getResultCode().value());
        assertEquals("사업자만 관리할 수 있습니다.", exception.getMsg());
    }

    @Test
    @DisplayName("객실 수정 실패 - 호텔 소유주가 아닐 경우")
    public void modifyRoomFailed_notEqualBusiness() {
        Business newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com");
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);
        Set<String> roomOptions = this.roomOptionService.findAll()
                .stream()
                .map(RoomOption::getName)
                .collect(Collectors.toSet());

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();

        roomOptions = new HashSet<>(Set.of("미니 냉장고"));
        PutRoomRequest putReq1 = new PutRoomRequest("수정 객실1", 5, null, null, 5, null,
                "in_booking", null, null, roomOptions);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.modifyRoom(hotel.getId(), roomId, newBusiness.getMember(), putReq1);
        });

        assertEquals(403, exception.getResultCode().value());
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.getMsg());
    }

    @Test
    @DisplayName("객실 삭제")
    public void deleteRoom() {
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();

        Set<String> roomOptions = new HashSet<>(Set.of("객실 내 금고", "미니 냉장고"));

        Map<String, Integer> bedTypeNumber = Map.of("SINGLE", 4, "DOUBLE", 2, "KING", 1);

        PostRoomRequest req1 = new PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, null, roomOptions);

        PostRoomResponse res1 = this.roomService.createRoom(hotel.getId(), business.getMember(), req1);

        Long roomId = res1.roomId();
        Room room = this.roomRepository.findById(roomId).get();

        this.roomService.deleteRoom(hotel.getId(), roomId, business.getMember());

        assertEquals(RoomStatus.UNAVAILABLE, room.getRoomStatus());
    }

    @Test
    @DisplayName("객실 삭제 실패 - 사업자가 아닐 경우")
    public void deleteRoomFailed_notBusiness() {
        Member member = this.getActor();
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();
        Long roomId = hotel.getRooms().getFirst().getId();

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.deleteRoom(hotel.getId(), roomId, member);
        });

        assertEquals(403, exception.getResultCode().value());
        assertEquals("사업자만 관리할 수 있습니다.", exception.getMsg());
    }

    @Test
    @DisplayName("객실 삭제 실패 - 호텔 소유주가 아닐 경우")
    public void deleteRoomFailed_notEqualBusiness() {
        Business newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com");
        Business business = this.getBusiness();
        Hotel hotel = this.hotelRepository.findByBusiness(business).get();
        Long roomId = hotel.getRooms().getFirst().getId();

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            this.roomService.deleteRoom(hotel.getId(), roomId, newBusiness.getMember());
        });

        assertEquals(403, exception.getResultCode().value());
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.getMsg());
    }

    // business1 비즈니스 호출
    private Business getBusiness() {
        Member actor = this.memberRepository.findByMemberName("business1").get();
        return actor.getBusiness();
    }

    private Member getActor() {
        return this.memberRepository.findByMemberName("customer1").get();
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
package com.ll.hotel.domain.hotel.hotel.service

import com.ll.hotel.domain.hotel.hotel.dto.PostHotelRequest
import com.ll.hotel.domain.hotel.hotel.dto.PutHotelRequest
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.hotel.option.service.HotelOptionService
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.hotel.room.repository.RoomRepository
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ServiceException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
internal class HotelServiceTest {
    @Autowired
    lateinit var hotelService: HotelService

    @Autowired
    lateinit var hotelOptionService: HotelOptionService

    @Autowired
    lateinit var hotelRepository: HotelRepository

    @Autowired
    lateinit var roomRepository: RoomRepository

    @Autowired
    lateinit var businessRepository: BusinessRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("호텔 생성")
    fun createHotel() {
        // given
        val business = this.createBusiness("새사장1", "newBusiness1@gmail.com")
        val actor = business.member
        val hotelOptions = this.hotelOptionService.findAll()
            .map { it.name }
            .toSet()

        val postHotelRequest = PostHotelRequest(
            "호텔3", "hotel3@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔3입니다.", hotelOptions = hotelOptions
        )

        // when
        val postHotelResponse = this.hotelService.createHotel(actor, postHotelRequest)

        val hotel = this.hotelRepository.findById(postHotelResponse.hotelId).get()

        business.hotel = hotel
        this.businessRepository.save(business)

        // then
        assertEquals(this.hotelRepository.count(), 2L)
        assertEquals(hotel.hotelName, "호텔3")
        assertEquals(hotel.hotelEmail, "hotel3@naver.com")
        assertEquals(hotel.hotelPhoneNumber, "010-1234-1234")
        assertEquals(hotel.business.id, business.id)
        assertEquals(hotel.business.member.role, Role.BUSINESS)
        assertEquals(hotel.business.hotel, hotel)
        assertEquals(hotel.hotelOptions.size, 2)

        val hotelNames = hotel.hotelOptions
            .map { it.name }
            .toSet()

        assertTrue(hotelNames.contains("무료 Wi-Fi"))
        assertTrue(hotelNames.contains("프론트 데스크"))
    }

    @Test
    @DisplayName("호텔 생성 실패 - 비사업가 호텔 생성 시도")
    fun createHotelFailed_notBusiness() {
        // given
        val actor = memberRepository.findByMemberName("customer1").get()

        val hotelOptions = emptySet<String>()

        val postHotelRequest = PostHotelRequest(
            "호텔1", "hotel@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", hotelOptions = hotelOptions
        )

        // when
        val exception = assertThrows<ServiceException> {
            this.hotelService.createHotel(actor, postHotelRequest)
        }

        // then
        assertEquals(403, exception.resultCode.value())
        assertEquals("사업자만 관리할 수 있습니다.", exception.msg)
    }

    @Test
    @DisplayName("호텔 생성 실패 - 사업가 1개 이상 호텔 생성 시도")
    fun createHotelFailed_severalCreate() {
        // given
        val actor = this.memberRepository.findByMemberName("business1").get()

        val hotelOptions = emptySet<String>()

        val postHotelRequest = PostHotelRequest(
            "호텔1", "hotel@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", hotelOptions = hotelOptions
        )

        // when
        val exception = assertThrows<ServiceException> {
            hotelService.createHotel(actor, postHotelRequest)
        }

        // then
        assertEquals(409, exception.resultCode.value())
        assertEquals("한 사업자는 하나의 호텔만 등록할 수 있습니다.", exception.msg)
    }

    @Test
    @DisplayName("호텔 생성 실패 - 존재하지 않는 호텔 옵션")
    fun createHotel_invalidHotelOptions() {
        // given
        val business = this.createBusiness("새사장1", "newBusiness1@gmail.com")
        val actor = business.member

        val hotelOptions = setOf("Parking_lot", "Breakfast", "Lunch")

        val postHotelRequest = PostHotelRequest(
            "호텔1", "hotel@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다.", hotelOptions = hotelOptions
        )

        // when
        val exception = assertThrows<ServiceException> {
            this.hotelService.createHotel(actor, postHotelRequest)
        }

        // then
        assertEquals(404, exception.resultCode.value())
        assertEquals("사용할 수 없는 호텔 옵션이 존재합니다.", exception.msg)
    }

    @Test
    @DisplayName("호텔 전체 목록 조회")
    fun findAllHotels() {
        // given_1
        var business = this.createBusiness("새사장1", "newHotel1@gmail.com")

        val req1 = PostHotelRequest(
            "호텔1", "hotel@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다."
        )

        var hotelRes = this.hotelService.createHotel(business.member, req1)
        var hotel = this.hotelRepository.findById(hotelRes.hotelId).get()

        var room = Room("새객실1", 3, hotel = hotel, standardNumber = 2, maxNumber = 6, basePrice = 30000)
        this.roomRepository.save(room)

        var rooms = hotel.rooms
        rooms.add(room)
        hotel.rooms = rooms
        this.hotelRepository.save(hotel)

        // given_2
        business = this.createBusiness("새사장2", "newHotel2@gmail.com")

        val req2 = PostHotelRequest(
            "호텔2", "sin@naver.com",
            "010-1111-1111", "부산시", 1111,
            5, LocalTime.of(14, 0), LocalTime.of(16, 0), "신호텔"
        )

        hotelRes = this.hotelService.createHotel(business.member, req2)
        hotel = this.hotelRepository.findById(hotelRes.hotelId).get()

        room = Room("새객실2", 3, hotel = hotel, standardNumber = 2, maxNumber = 6, basePrice = 50000)
        this.roomRepository.save(room)

        rooms = hotel.rooms
        rooms.add(room)
        hotel.rooms = rooms
        this.hotelRepository.save(hotel)

        // when
        val resultPage =
            this.hotelService.findAllHotels(
                1, 10, "latest", "asc", "",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(31), 2
            )
        val list = resultPage.content
        val resFirst = list.first()
        val resLast = list.last()

        // then
        assertEquals(this.hotelRepository.findAll().size, list.size)
        assertEquals(resFirst.hotelName, "강남호텔")
        assertEquals(resLast.hotelName, "호텔2")
        assertEquals(resFirst.streetAddress, "서울시 강남구 호텔로 10")
        assertEquals(resLast.streetAddress, "부산시")
    }

    @Test
    @DisplayName("호텔 전체 목록 조회 - filterDirection 값을 입력하지 않았을 경우")
    fun findAllHotelsWithoutFilterDirection() {
        // given_1
        var business = this.createBusiness("새사장1", "newHotel1@gmail.com")

        val req1 = PostHotelRequest(
            "호텔1", "hotel@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다."
        )

        var hotelRes = this.hotelService.createHotel(business.member, req1)
        var hotel = this.hotelRepository.findById(hotelRes.hotelId).get()

        var room = Room("새객실1", 3, hotel = hotel, standardNumber = 2, maxNumber = 6, basePrice = 30000)
        this.roomRepository.save(room)

        var rooms = hotel.rooms
        rooms.add(room)
        hotel.rooms = rooms
        this.hotelRepository.save(hotel)

        // given_2
        business = this.createBusiness("새사장2", "newHotel2@gmail.com")

        val req2 = PostHotelRequest(
            "호텔2", "sin@naver.com",
            "010-1111-1111", "부산시", 1111,
            5, LocalTime.of(14, 0), LocalTime.of(16, 0), "신호텔"
        )

        hotelRes = this.hotelService.createHotel(business.member, req2)
        hotel = this.hotelRepository.findById(hotelRes.hotelId).get()

        room = Room("새객실2", 3, hotel = hotel, standardNumber = 2, maxNumber = 6, basePrice = 50000)
        this.roomRepository.save(room)

        rooms = hotel.rooms
        rooms.add(room)
        hotel.rooms = rooms
        this.hotelRepository.save(hotel)

        // when
        val resultPage =
            this.hotelService.findAllHotels(
                1, 10, "latest", "asc", "",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(31), 2
            )
        val list = resultPage.content
        val resFirst = list.first()
        val resLast = list.last()

        // then
        assertEquals(this.hotelRepository.findAll().size, list.size)
        assertEquals(resFirst.hotelName, "강남호텔")
        assertEquals(resLast.hotelName, "호텔2")
        assertEquals(resFirst.streetAddress, "서울시 강남구 호텔로 10")
        assertEquals(resLast.streetAddress, "부산시")
    }

    @Test
    @DisplayName("호텔 전체 목록 조회 - 주소지 검색")
    fun findAllHotelsWithStreetAddress() {
        // given_1
        var business = this.createBusiness("새사장1", "newHotel1@gmail.com")

        val req1 = PostHotelRequest(
            "호텔1", "hotel@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다."
        )

        var hotelRes = this.hotelService.createHotel(business.member, req1)
        var hotel = this.hotelRepository.findById(hotelRes.hotelId).get()

        var room = Room("새객실1", 3, hotel = hotel, standardNumber = 2, maxNumber = 6, basePrice = 30000)
        this.roomRepository.save(room)

        var rooms = hotel.rooms
        rooms.add(room)
        hotel.rooms = rooms
        this.hotelRepository.save(hotel)

        // given_2
        business = this.createBusiness("새사장2", "newHotel2@gmail.com")

        val req2 = PostHotelRequest(
            "호텔2", "sin@naver.com",
            "010-1111-1111", "부산시", 1111,
            5, LocalTime.of(14, 0), LocalTime.of(16, 0), "신호텔"
        )

        hotelRes = this.hotelService.createHotel(business.member, req2)
        hotel = this.hotelRepository.findById(hotelRes.hotelId).get()

        room = Room("새객실2", 3, hotel = hotel, standardNumber = 2, maxNumber = 6, basePrice = 50000)
        this.roomRepository.save(room)

        rooms = hotel.rooms
        rooms.add(room)
        hotel.rooms = rooms
        this.hotelRepository.save(hotel)

        // when
        val resultPage =
            this.hotelService.findAllHotels(
                1, 10, "latest", "asc", "서울",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(31), 2
            )
        val list = resultPage.content
        val resFirst = list.first()
        val resLast = list.last()

        // then
        assertEquals(2, list.size)
        assertEquals(resFirst.hotelName, "강남호텔")
        assertEquals(resLast.hotelName, "호텔1")
        assertEquals(resFirst.streetAddress, "서울시 강남구 호텔로 10")
        assertEquals(resLast.streetAddress, "서울시")
    }

    @Test
    @DisplayName("호텔 단일 목록 조회")
    fun findHotelDetail() {
        // given_1
        val actor = this.memberRepository.findByMemberName("business1").get()
        var business: Business? = businessRepository.findByMember(actor)
        var hotel = hotelRepository.findByBusiness(business!!)

        // when_1
        var detRes = hotelService.findHotelDetail(hotel!!.id)

        // then_1
        assertEquals(hotel.id, detRes.hotelDetailDto.hotelId)
        assertEquals("강남호텔", detRes.hotelDetailDto.hotelName)
        assertEquals("서울시 강남구 호텔로 10", detRes.hotelDetailDto.streetAddress)
        assertTrue(detRes.hotelDetailDto.hotelOptions.contains("무료 Wi-Fi"))
        assertTrue(detRes.hotelDetailDto.hotelOptions.contains("프론트 데스크"))

        // given_2
        business = this.createBusiness("새사장1", "newHotel1@gmail.com")

        val req1 = PostHotelRequest(
            "호텔1", "hotel@naver.com",
            "010-1234-1234", "서울시", 83,
            3, LocalTime.of(12, 0), LocalTime.of(14, 0), "호텔입니다."
        )

        val res1 = this.hotelService.createHotel(business.member, req1)
        hotel = this.hotelRepository.findById(res1.hotelId).get()

        business.hotel = hotel
        this.businessRepository.save(business)

        // when_2
        detRes = this.hotelService.findHotelDetail(hotel.id)

        // then_2
        assertEquals(res1.hotelId, detRes.hotelDetailDto.hotelId)
        assertEquals("호텔1", detRes.hotelDetailDto.hotelName)
        assertEquals("서울시", detRes.hotelDetailDto.streetAddress)
        assertEquals(detRes.hotelDetailDto.hotelOptions.size, 0)
    }

    @Test
    @DisplayName("호텔 수정")
    fun modifyHotel() {
        // given
        val actor = memberRepository.findByMemberName("business1").get()
        val business: Business? = businessRepository.findByMember(actor)
        val hotelOptions = setOf("무료 Wi-Fi")

        var hotel = hotelRepository.findByBusiness(business!!)
        val hotelId = hotel!!.id

        val req1 = PutHotelRequest(
            "수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 83, 1,
            LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name, hotelOptions = hotelOptions
        )

        // when
        val res1 = this.hotelService.modifyHotel(hotelId, actor, req1)

        hotel = this.hotelRepository.findById(hotelId).get()

        val hotelOptionNames = hotel.hotelOptions
            .map { it.name }
            .toSet()

        // then
        assertEquals(hotel.id, res1.hotelId)
        assertEquals(hotel.streetAddress, req1.streetAddress)
        assertEquals(hotel.zipCode, req1.zipCode)
        assertEquals(hotel.checkInTime, req1.checkInTime)
        assertEquals(hotel.hotelName, res1.hotelName)
        assertEquals(hotel.hotelEmail, req1.hotelEmail)
        assertEquals(1, hotelOptionNames.size)
        assertTrue(hotelOptionNames.contains("무료 Wi-Fi"))
        assertFalse(hotelOptionNames.contains("프론트 데스크"))
    }

    @Test
    @DisplayName("호텔 수정 실패 - 사업자가 아닐 경우")
    fun modifyHotelFailed_notBusiness() {
        // given
        val actor = this.memberRepository.findByMemberName("customer1").get()

        val businessMem = this.memberRepository.findByMemberName("business1").get()
        val business: Business? = this.businessRepository.findByMember(businessMem)
        val hotelOptions = emptySet<String>()

        val hotel = this.hotelRepository.findByBusiness(business!!)

        val req1 = PutHotelRequest(
            "수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 83, 1,
            LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name, hotelOptions = hotelOptions
        )

        // when
        val exception = assertThrows<ServiceException> {
            hotelService.modifyHotel(hotel!!.id, actor, req1)
        }

        // then
        assertEquals(403, exception.resultCode.value())
        assertEquals("사업자만 관리할 수 있습니다.", exception.msg)
    }

    @Test
    @DisplayName("호텔 수정 실패 - 호텔 소유주가 아닐 경우")
    fun modifyHotelFailed_notEqualBusiness() {
        // given
        val newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com")

        val businessMem = this.memberRepository.findByMemberName("business1").get()
        val business: Business? = this.businessRepository.findByMember(businessMem)
        val hotelOptions = emptySet<String>()

        val hotel = hotelRepository.findByBusiness(business!!)
        val hotelId = hotel!!.id

        val req1 = PutHotelRequest(
            "수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 83, 1,
            LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name, hotelOptions = hotelOptions
        )

        // when
        val exception = assertThrows<ServiceException> {
            this.hotelService.modifyHotel(hotelId, newBusiness.member, req1)
        }

        // then
        assertEquals(403, exception.resultCode.value())
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.msg)
    }

    @Test
    @DisplayName("호텔 수정 실패 - 존재하지 않는 호텔 옵션")
    fun modifyHotelFailed_invalidHotelOptions() {
        // given
        val actor = this.memberRepository.findByMemberName("business1").get()
        val business: Business? = this.businessRepository.findByMember(actor)
        val hotelOptions = setOf("에어컨")

        val hotel = hotelRepository.findByBusiness(business!!)
        val hotelId = hotel!!.id

        val req1 = PutHotelRequest(
            "수정된 호텔1", "moHotel@naver.com", "010-1111-2222", "", 83, 1,
            LocalTime.now(), LocalTime.now(), "", HotelStatus.AVAILABLE.name, hotelOptions = hotelOptions
        )

        // when
        val exception = assertThrows<ServiceException> {
            this.hotelService.modifyHotel(hotelId, actor, req1)
        }

        // then
        assertEquals(404, exception.resultCode.value())
        assertEquals("사용할 수 없는 호텔 옵션이 존재합니다.", exception.msg)
    }

    @Test
    @DisplayName("호텔 삭제")
    fun deleteHotel() {
        // given
        val actor = this.memberRepository.findByMemberName("business1").get()
        val business: Business? = this.businessRepository.findByMember(actor)
        val hotel = this.hotelRepository.findByBusiness(business!!)

        // when
        this.hotelService.deleteHotel(hotel!!.id, actor)

        // then
        assertEquals(HotelStatus.UNAVAILABLE, hotel.hotelStatus)
    }

    @Test
    @DisplayName("호텔 삭제 실패 - 사업자가 아닐 경우")
    fun deleteHotelFailed_notBusiness() {
        // given
        val actor = this.memberRepository.findByMemberName("customer1").get()

        val businessMem = this.memberRepository.findByMemberName("business1").get()
        val business: Business? = this.businessRepository.findByMember(businessMem)

        val hotel = this.hotelRepository.findByBusiness(business!!)

        // when
        val exception = assertThrows<ServiceException> {
            this.hotelService.deleteHotel(hotel!!.id, actor)
        }

        // then
        assertEquals(403, exception.resultCode.value())
        assertEquals("사업자만 관리할 수 있습니다.", exception.msg)
    }

    @Test
    @DisplayName("호텔 삭제 실패 - 호텔 소유주가 아닐 경우")
    fun deleteHotelFailed_notEqualBusiness() {
        // given
        val newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com")

        val actor = this.memberRepository.findByMemberName("business1").get()
        val business: Business? = this.businessRepository.findByMember(actor)

        val hotel = this.hotelRepository.findByBusiness(business!!)

        // when
        val exception = assertThrows<ServiceException> {
            this.hotelService.deleteHotel(hotel!!.id, newBusiness.member)
        }

        // then
        assertEquals(403, exception.resultCode.value())
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.msg)
    }

    // 사업가 생성
    private fun createBusiness(name: String, email: String): Business {
        // 회원 생성
        val member = Member(email, name, "01011111111", LocalDate.now(), Role.BUSINESS, MemberStatus.ACTIVE)

        // 회원 저장
        this.memberRepository.save(member)

        // 사업가 등록
        val business = Business(
            this.createRegistrationNumber(),
            LocalDate.now(),
            name,
            BusinessApprovalStatus.APPROVED,
            member,
            null
        )

        // 사업가 저장
        this.businessRepository.save(business)

        return business
    }

    // 사업가 번호 난수 생성
    private fun createRegistrationNumber(): String {
        // 1000000000 ~ 9999999999 난수 생성
        return ((Math.random() * 9000000000L) + 1000000000L).toLong().toString()
    }
}
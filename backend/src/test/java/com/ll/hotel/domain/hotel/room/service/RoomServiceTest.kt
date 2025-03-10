package com.ll.hotel.domain.hotel.room.service

import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.option.entity.RoomOption
import com.ll.hotel.domain.hotel.option.service.RoomOptionService
import com.ll.hotel.domain.hotel.room.dto.GetRoomResponse
import com.ll.hotel.domain.hotel.room.dto.PostRoomRequest
import com.ll.hotel.domain.hotel.room.dto.PutRoomRequest
import com.ll.hotel.domain.hotel.room.repository.RoomRepository
import com.ll.hotel.domain.hotel.room.type.RoomStatus
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
import java.util.stream.Collectors

@SpringBootTest
@ActiveProfiles("test")
@Transactional
internal class RoomServiceTest {

    @Autowired
    lateinit var roomService: RoomService

    @Autowired
    lateinit var roomOptionService: RoomOptionService

    @Autowired
    lateinit var hotelRepository: HotelRepository

    @Autowired
    lateinit var roomRepository: RoomRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var businessRepository: BusinessRepository

    @Test
    @DisplayName("객실 생성")
    fun createRoom() {
        val business: Business? = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val roomOptions = setOf("객실 내 금고", "미니 냉장고")
        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        val roomId: Long = res1.roomId
        val room = this.roomRepository.findById(roomId).get()

        val roomOption = room.roomOptions.stream().map(RoomOption::name).collect(Collectors.toSet())

        assertEquals(room.roomName, "객실1")
        assertEquals(hotel.id, res1.hotelId)
        assertEquals(room.basePrice, 300000)
        assertEquals(room.bedTypeNumber.bedSingle, 4)
        assertEquals(room.bedTypeNumber.bedDouble, 2)
        assertEquals(room.bedTypeNumber.bedKing, 1)
        assertEquals(room.bedTypeNumber.bedTriple, 0)
        assertEquals(room.standardNumber, 2)
        assertEquals(roomOption.size, 2)
        assertTrue(roomOption.contains("객실 내 금고"))
        assertTrue(roomOption.contains("미니 냉장고"))
    }

    @Test
    @DisplayName("객실 생성 실패 - 존재하지 않는 객실 옵션")
    fun createRoomFailed_invalidRoomOptions() {
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val roomOptions = setOf("청소기")
        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val exception = assertThrows<ServiceException> {
            this.roomService.createRoom(hotel!!.id, business.member, req1)
        }

        assertEquals(404, exception.resultCode.value())
        assertEquals("사용할 수 없는 객실 옵션이 존재합니다.", exception.msg)
    }

    @Test
    @DisplayName("객실 생성 실패 - 사업가가 아닐 경우")
    fun createRoomFailed_notBusiness() {
        val actor = this.actor
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val roomOptions = setOf("객실 내 금고", "미니 냉장고")
        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val exception = assertThrows<ServiceException> {
            this.roomService.createRoom(hotel!!.id, actor, req1)
        }

        assertEquals(403, exception.resultCode.value())
        assertEquals("사업자만 관리할 수 있습니다.", exception.msg)
    }

    @Test
    @DisplayName("객실 생성 실패 - 호텔 소유주가 아닐 경우")
    fun createRoomFailed_notEqualBusiness() {
        val newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com")
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val roomOptions = setOf("객실 내 금고", "미니 냉장고")
        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val exception = assertThrows<ServiceException> {
            this.roomService.createRoom(hotel!!.id, newBusiness.member, req1)
        }

        assertEquals(403, exception.resultCode.value())
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.msg)
    }

    @Test
    @DisplayName("객실 전체 조회")
    fun findAllRooms() {
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val roomOptions = setOf("객실 내 금고", "미니 냉장고")
        var bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        val roomId: Long = res1.roomId

        bedTypeNumber = mapOf("DOUBLE" to 4, "QUEEN" to 1)
        val req2 = PostRoomRequest("객실2", 2, 500000, 3, 4, bedTypeNumber)

        val res2 = this.roomService.createRoom(hotel.id, business.member, req2)

        val rooms = this.roomService.findAllRooms(hotel.id)
        var res: GetRoomResponse = rooms[1]

        assertEquals(rooms.size, 3)
        assertEquals(res.roomId, roomId)
        assertEquals(res.roomName, "객실1")
        assertEquals(res.basePrice, 300000)
        assertEquals(res.bedTypeNumber, 4)
        assertEquals(res.bedTypeNumber.bedDouble, 2)
        assertEquals(res.bedTypeNumber.bedKing, 1)
        assertEquals(res.bedTypeNumber.bedTriple, 0)
        assertEquals(res.standardNumber, 2)

        res = rooms[2]

        assertEquals(res2.roomId, roomId + 1)
        assertEquals(res.roomName, "객실2")
        assertEquals(res.basePrice, 500000)
        assertEquals(res.bedTypeNumber.bedDouble, 4)
        assertEquals(res.bedTypeNumber.bedQueen, 1)
        assertEquals(res.bedTypeNumber.bedTriple, 0)
        assertEquals(res.standardNumber, 3)
    }

    @Test
    @DisplayName("특정 객실 조회")
    fun findRoom() {
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val roomOptions = setOf("객실 내 금고", "미니 냉장고")
        var bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        var roomId: Long = res1.roomId

        bedTypeNumber = mapOf("DOUBLE" to 4, "QUEEN" to 1)
        val req2 = PostRoomRequest("객실2", 2, 500000, 3, 4, bedTypeNumber)

        val res2 = this.roomService.createRoom(hotel.id, business.member, req2)

        var detRes1 = this.roomService.findRoomDetail(hotel.id, roomId)

        assertEquals(detRes1.roomDto.id, roomId)
        assertEquals(detRes1.roomDto.hotelId, hotel.id)
        assertEquals(detRes1.roomDto.roomName, "객실1")
        assertEquals(detRes1.roomDto.roomNumber, req1.roomNumber)
        assertEquals(detRes1.roomDto.basePrice, req1.basePrice)
        assertEquals(detRes1.roomDto.bedTypeNumber.bedSingle, req1.bedTypeNumber.get("SINGLE"))
        assertEquals(detRes1.roomDto.bedTypeNumber.bedDouble, req1.bedTypeNumber.get("DOUBLE"))
        assertEquals(detRes1.roomDto.bedTypeNumber.bedKing, req1.bedTypeNumber.get("KING"))
        assertEquals(detRes1.roomDto.bedTypeNumber.bedTriple, 0)
        assertEquals(detRes1.roomDto.roomStatus, RoomStatus.AVAILABLE.name)
        assertEquals(detRes1.roomImageUrls.size, 0)
        assertEquals(detRes1.roomDto.roomOptions.size, 2)
        assertEquals(detRes1.roomDto.standardNumber, 2)

        roomId = res2.roomId
        detRes1 = roomService.findRoomDetail(hotel.id, roomId)

        assertEquals(detRes1.roomDto.id, roomId)
        assertEquals(detRes1.roomDto.hotelId, hotel.id)
        assertEquals(detRes1.roomDto.roomName, "객실2")
        assertEquals(detRes1.roomDto.roomNumber, req2.roomNumber)
        assertEquals(detRes1.roomDto.basePrice, req2.basePrice)
        assertEquals(detRes1.roomDto.bedTypeNumber.bedDouble, req2.bedTypeNumber.get("DOUBLE"))
        assertEquals(detRes1.roomDto.bedTypeNumber.bedQueen, req2.bedTypeNumber.get("QUEEN"))
        assertEquals(detRes1.roomDto.bedTypeNumber.bedTriple, 0)
        assertEquals(detRes1.roomDto.roomStatus, RoomStatus.AVAILABLE.name)
        assertEquals(detRes1.roomImageUrls.size, 0)
        assertEquals(detRes1.roomDto.roomOptions.size, 0)
        assertEquals(detRes1.roomDto.standardNumber, 3)
    }

    @Test
    @DisplayName("객실 수정")
    fun modifyRoom() {
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)
        var roomOptions: Set<String> = this.roomOptionService.findAll()
            .map { it.name }
            .toSet()

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        val roomId: Long = res1.roomId
        var room = this.roomRepository.findById(roomId).get()

        roomOptions = setOf("미니 냉장고")
        val putReq1 = PutRoomRequest(
            "수정 객실1", 5, 30000, 1, 5,
            roomStatus = "in_booking", roomOptions = roomOptions
        )

        val putRes1 = this.roomService.modifyRoom(hotel.id, roomId, business.member, putReq1)

        assertEquals(putRes1.hotelId, hotel.id)
        assertEquals(putRes1.roomId, room.id)
        assertEquals(putRes1.roomName, room.roomName)
        assertEquals(putRes1.roomStatus, RoomStatus.IN_BOOKING.value)

        room = this.roomRepository.findById(roomId).get()

        val roomNames = room.roomOptions
            .map { it.name }
            .toSet()

        assertEquals(room.roomName, putReq1.roomName)
        assertEquals(room.roomNumber, putReq1.roomNumber)
        assertEquals(room.basePrice, putReq1.basePrice)
        assertEquals(room.standardNumber, putReq1.standardNumber)
        assertEquals(room.maxNumber, putReq1.maxNumber)
        assertEquals(room.bedTypeNumber, putReq1.bedTypeNumber)
        assertEquals(room.roomStatus, RoomStatus.IN_BOOKING)
        assertEquals(room.hotel.id, hotel.id)
        assertEquals(1, roomOptions.size)
        assertEquals(roomNames, roomOptions)
    }

    @Test
    @DisplayName("객실 수정 실패 - 존재하지 않는 객실 옵션")
    fun modifyRoomFailed_invalidRoomOption() {
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = emptySet())

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        val roomId: Long = res1.roomId

        val roomOptions = setOf("TV", "AirConditioner")
        val putReq1 = PutRoomRequest(
            "수정 객실1", 5, 30000, 1, 5,
            roomStatus = "in_booking", roomOptions = roomOptions
        )

        val exception = assertThrows<ServiceException> {
            this.roomService.modifyRoom(hotel.id, roomId, business.member, putReq1)
        }

        assertEquals(404, exception.resultCode.value())
        assertEquals("사용할 수 없는 객실 옵션이 존재합니다.", exception.msg)
    }

    @Test
    @DisplayName("객실 수정 실패 - 사업자가 아닐 경우")
    fun modifyRoomFailed_notBusiness() {
        val actor = this.actor
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)
        var roomOptions = this.roomOptionService.findAll()
            .map { it.name }
            .toSet()

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        val roomId: Long = res1.roomId

        roomOptions = setOf("미니 냉장고")
        val putReq1 = PutRoomRequest(
            "수정 객실1", 5, 30000, 1, 5,
            roomStatus = "in_booking", roomOptions = roomOptions
        )

        val exception = assertThrows<ServiceException> {
            this.roomService.modifyRoom(hotel.id, roomId, actor, putReq1)
        }

        assertEquals(403, exception.resultCode.value())
        assertEquals("사업자만 관리할 수 있습니다.", exception.msg)
    }

    @Test
    @DisplayName("객실 수정 실패 - 호텔 소유주가 아닐 경우")
    fun modifyRoomFailed_notEqualBusiness() {
        val newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com")
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)
        var roomOptions = this.roomOptionService.findAll()
            .map { it.name }
            .toSet()

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        val roomId: Long = res1.roomId

        roomOptions = setOf("미니 냉장고")
        val putReq1 = PutRoomRequest(
            "수정 객실1", 5, 30000, 1, 5,
            roomStatus = "in_booking", roomOptions = roomOptions
        )

        val exception = assertThrows<ServiceException> {
            this.roomService.modifyRoom(hotel.id, roomId, newBusiness.member, putReq1)
        }

        assertEquals(403, exception.resultCode.value())
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.msg)
    }

    @Test
    @DisplayName("객실 삭제")
    fun deleteRoom() {
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)

        val roomOptions = setOf("객실 내 금고", "미니 냉장고")

        val bedTypeNumber = mapOf("SINGLE" to 4, "DOUBLE" to 2, "KING" to 1)

        val req1 = PostRoomRequest("객실1", 1, 300000, 2, 4, bedTypeNumber, roomOptions = roomOptions)

        val res1 = this.roomService.createRoom(hotel!!.id, business.member, req1)

        val roomId: Long = res1.roomId
        val room = this.roomRepository.findById(roomId).get()

        this.roomService.deleteRoom(hotel.id, roomId, business.member)

        assertEquals(RoomStatus.UNAVAILABLE, room.roomStatus)
    }

    @Test
    @DisplayName("객실 삭제 실패 - 사업자가 아닐 경우")
    fun deleteRoomFailed_notBusiness() {
        val member = this.actor
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)
        val roomId = hotel!!.rooms.first().id

        val exception = assertThrows<ServiceException> {
            this.roomService.deleteRoom(hotel.id, roomId, member)
        }

        assertEquals(403, exception.resultCode.value())
        assertEquals("사업자만 관리할 수 있습니다.", exception.msg)
    }

    @Test
    @DisplayName("객실 삭제 실패 - 호텔 소유주가 아닐 경우")
    fun deleteRoomFailed_notEqualBusiness() {
        val newBusiness = this.createBusiness("새사장1", "newHotel1@gmail.com")
        val business = this.business
        val hotel = this.hotelRepository.findByBusiness(business!!)
        val roomId = hotel!!.rooms.first().id

        val exception = assertThrows<ServiceException> {
            this.roomService.deleteRoom(hotel.id, roomId, newBusiness.member)
        }

        assertEquals(403, exception.resultCode.value())
        assertEquals("해당 호텔의 사업자가 아닙니다.", exception.msg)
    }

    private val business: Business?
        // business1 비즈니스 호출
        get() {
            val actor = this.memberRepository.findByMemberName("business1").get()
            return actor.business
        }

    private val actor: Member
        get() = this.memberRepository.findByMemberName("customer1").get()

    // 사업가 생성
    private fun createBusiness(name: String, email: String): Business {
        // 회원 생성
        val member = Member(email, name, "01011111111", LocalDate.now(), Role.BUSINESS, MemberStatus.ACTIVE)

        // 회원 저장
        this.memberRepository.save(member)

        // 사업가 등록
        val business =
            Business(this.createRegistrationNumber(), LocalDate.now(), name, BusinessApprovalStatus.APPROVED, member)

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
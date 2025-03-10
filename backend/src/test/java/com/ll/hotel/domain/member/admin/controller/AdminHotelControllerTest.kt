package com.ll.hotel.domain.member.admin.controller

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.domain.member.member.type.MemberStatus
import jakarta.transaction.Transactional
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = ["ADMIN"])
class AdminHotelControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val hotelRepository: HotelRepository,
    private val businessRepository : BusinessRepository,
    private val memberRepository: MemberRepository
) {
    private var testId: Long = 0L

    @BeforeEach
    fun setUp() {
        memberRepository.deleteAll()

        val member = memberRepository.save(
            Member(
                birthDate = LocalDate.now(),
                memberEmail = "member@email.com",
                memberName = "member",
                memberPhoneNumber = "01012345678",
                memberStatus = MemberStatus.ACTIVE,
                role = Role.BUSINESS
            )
        )

        val business = businessRepository.save(
            Business(
                businessRegistrationNumber = "1234567890",
                startDate = LocalDate.now(),
                ownerName = "홍길동",
                approvalStatus = BusinessApprovalStatus.PENDING,
                member = member
            )
        )

        val hotel = hotelRepository.save(
            Hotel(
                hotelName = "호텔",
                hotelExplainContent = "최상급 호텔",
                hotelEmail = "hotel@gmail.com",
                zipCode = 42153,
                streetAddress = "어쩌구로 15번길",
                hotelGrade = 5,
                hotelStatus = HotelStatus.PENDING,
                checkInTime = LocalTime.of(15, 0),
                checkOutTime = LocalTime.of(11, 0),
                hotelPhoneNumber = "01012345678",
                averageRating = 5.6,
                totalReviewCount = 3L,
                totalReviewRatingSum = 5L,
                business = business
            )
        )
        testId = hotel.id
    }

    @Test
    @DisplayName("호텔 페이지 조회")
    fun `should return paged list of hotels`() {
        mockMvc.perform(
            get("/api/admin/hotels")
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminHotelController::class.java))
            .andExpect(handler().methodName("getAll"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }

    @Test
    @DisplayName("호텔 페이지 조회 - 잘못된 페이지를 요청한 경우")
    fun `should throw exception when invalid page requested`() {
        val pageSize = 10
        val totalElements = hotelRepository.count()
        val invalidPage = (totalElements / pageSize).toInt() + 1

        mockMvc.perform(
            get("/api/admin/hotels?page={page}", invalidPage)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminHotelController::class.java))
            .andExpect(handler().methodName("getAll"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.BAD_REQUEST.name)))
    }

    @Test
    @DisplayName("호텔 조회")
    fun `should find hotel by id`() {
        mockMvc.perform(
            get("/api/admin/hotels/{id}", testId)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminHotelController::class.java))
            .andExpect(handler().methodName("getById"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }

    @Test
    @DisplayName("호텔 조회 - 요청이 잘못된 경우")
    fun `should throw exception when hotel not found`() {
        val invalidHotelId = hotelRepository.count() + 1

        mockMvc.perform(
            get("/api/admin/hotels/{id]", invalidHotelId)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminHotelController::class.java))
            .andExpect(handler().methodName("getById"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.NOT_FOUND.name)))
    }

    @Test
    @DisplayName("사업자 승인")
    fun `should approve hotel and update status`() {
        val requestBody =
            """
                {
                    "hotelStatus": "AVAILABLE"
                }
                """.trimIndent()

        mockMvc.perform(
            patch("/api/admin/hotels/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminHotelController::class.java))
            .andExpect(handler().methodName("approve"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }
}
package com.ll.hotel.domain.member.admin.controller

import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.domain.member.member.type.MemberStatus
import io.mockk.clearAllMocks
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = ["ADMIN"])
class AdminBusinessControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val businessRepository: BusinessRepository,
    private val memberRepository: MemberRepository
) {
    private var testId: Long = 0L

    @BeforeEach
    fun setUp() {
        clearAllMocks()
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
                ownerName = "김사장",
                approvalStatus = BusinessApprovalStatus.PENDING,
                member = member
            )
        )
        testId = business.id
    }

    @Test
    @DisplayName("사업자 페이지 조회")
    fun `should return paged list of businesses`() {

        mockMvc.perform(
            get("/api/admin/businesses")
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminBusinessController::class.java))
            .andExpect(handler().methodName("getAll"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }

    @Test
    @DisplayName("사업자 페이지 조회 - 잘못된 페이지를 요청한 경우")
    fun `should throw exception when invalid page requested`() {
        val pageSize = 10
        val totalElements = businessRepository.count()
        val invalidPage = (totalElements - 1) / pageSize + 1

        mockMvc.perform(
            get("/api/admin/businesses?page={page}", invalidPage)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminBusinessController::class.java))
            .andExpect(handler().methodName("getAll"))
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("사업자 조회")
    fun `should find business by id `() {
        mockMvc.perform(
            get("/api/admin/businesses/{id}", testId)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminBusinessController::class.java))
            .andExpect(handler().methodName("getById"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }

    @Test
    @DisplayName("사업자 조회 - 요청이 잘못된 경우")
    fun `should throw exception when business not found`() {
        mockMvc.perform(
            get("/api/admin/businesses/{id}", Long.MAX_VALUE)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminBusinessController::class.java))
            .andExpect(handler().methodName("getById"))
            .andExpect(status().isNotFound())
    }

    @Test
    @DisplayName("사업자 승인")
    fun `should approve business and update member role`() {
        val requestBody = """
                {
                    "businessApprovalStatus": "APPROVED"
                }
                """.trimIndent()

        mockMvc.perform(
            patch("/api/admin/businesses/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andDo(print())
            .andExpect(handler().handlerType(AdminBusinessController::class.java))
            .andExpect(handler().methodName("approve"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }
}
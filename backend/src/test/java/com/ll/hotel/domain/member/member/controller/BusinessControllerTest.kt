package com.ll.hotel.domain.member.member.controller

import com.ll.hotel.domain.member.member.dto.response.BusinessResponse
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.service.BusinessService
import com.ll.hotel.domain.member.member.service.BusinessValidationService
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.global.exceptions.handler.GlobalExceptionHandler
import com.ll.hotel.global.request.Rq
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(MockKExtension::class)
class BusinessControllerTest {
    private lateinit var mockMvc: MockMvc

    private val rq: Rq = mockk()
    private val businessValidationService = mockk<BusinessValidationService>()
    private val businessService = mockk<BusinessService>()

    @InjectMockKs
    private var businessController = BusinessController(businessService, rq)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockMvc = MockMvcBuilders
            .standaloneSetup(businessController)
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }
    @Test
    @DisplayName("유효한 사업자 등록")
    fun `should register valid business`() {
        // Given
        val mockMember = Member(
                memberName = "member",
                role = Role.BUSINESS
        )

        val mockBusiness = Business(
            businessRegistrationNumber = "1234567890",
            ownerName = "홍길동",
            startDate = LocalDate.of(2020, 1, 1),
            member = mockMember,
            approvalStatus = BusinessApprovalStatus.APPROVED
        )

        val requestBody = """
                {
                    "businessRegistrationNumber": "1234567890",
                    "startDate": "2020-01-01",
                    "ownerName": "홍길동"
                }
                """.trimIndent()

        // Mock 동작 설정
        every { rq.getActor() } returns mockMember
        every { businessService.register(any(), any()) } returns BusinessResponse.ApprovalResult.of(mockBusiness)

        // When & Then
        mockMvc.perform(
            post("/api/businesses/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value(HttpStatus.CREATED.name))

        verify { rq.getActor() }
        verify { businessService.register(any(), any()) }
    }
}
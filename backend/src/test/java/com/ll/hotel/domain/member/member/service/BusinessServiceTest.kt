package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.exceptions.ServiceException
import io.mockk.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessServiceTest (
    private val businessService: BusinessService,
    private val memberRepository: MemberRepository,
) {
    private val businessValidationService = mockk<BusinessValidationService>()
    private var testId: Long = 0L

    @BeforeEach
    fun setUp() {
        memberRepository.deleteAll()

        testId = memberRepository.save(
            Member(
                birthDate = LocalDate.now(),
                memberEmail = "member@email.com",
                memberName = "member",
                memberPhoneNumber = "01012345678",
                memberStatus = MemberStatus.ACTIVE,
                role = Role.BUSINESS
            )
        ).id
    }

    private fun createBusinessRequest() = BusinessRequest.RegistrationInfo(
        businessRegistrationNumber = "1234567890",
        startDate = LocalDate.now(),
        ownerName = "홍길동"
    )

    @Test
    @DisplayName("사업자 등록 - 01")
    fun `should register business successfully`() {
        // Given
        val businessRequest = createBusinessRequest()

        val member = memberRepository.findById(testId).get()
        every { businessValidationService.validateBusiness(any()) } just runs

        // When
        val result = businessService.register(businessRequest, member)

        // Then
        assertThat(result.approvalStatus).isEqualTo(BusinessApprovalStatus.APPROVED)
        assertThat(result.businessRegistrationNumber).isEqualTo(businessRequest.businessRegistrationNumber)

        verify { businessValidationService.validateBusiness(businessRequest) }
    }

    @Test
    @DisplayName("사업자 등록 실패 - 02")
    fun `should throw exception when business registration fails`() {
        // Given
        val businessRequest = createBusinessRequest()
        val member = memberRepository.findById(testId).get()

        every { businessValidationService.validateBusiness(any()) } answers {
            ErrorCode.INVALID_BUSINESS_INFO.throwServiceException()
        }

        // When
        val exception = assertThrows<ServiceException> {
            businessService.register(businessRequest, member)
        }

        // Then
        assertThat(exception.resultCode).isEqualTo(ErrorCode.INVALID_BUSINESS_INFO)

        verify { businessValidationService.validateBusiness(businessRequest) }
    }
}
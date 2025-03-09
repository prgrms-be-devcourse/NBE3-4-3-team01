package com.ll.hotel.domain.member.admin.service

import com.ll.hotel.domain.member.admin.dto.request.AdminBusinessRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminBusinessResponse
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.exceptions.ServiceException
import org.assertj.core.api.Assertions.assertThat
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
class AdminBusinessServiceTest(
    private val adminBusinessService: AdminBusinessService,
    private val businessRepository: BusinessRepository,
    private val memberRepository: MemberRepository
) {
    private var testId: Long = 0L

    @BeforeEach
    fun setUp() {
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
                ownerName = "홍길동",
                startDate = LocalDate.of(2020, 1, 1),
                member = member,
                approvalStatus = BusinessApprovalStatus.PENDING
            )
        )
        testId = business.id
        }

    @Test
    @DisplayName("사업자 페이지 조회")
    fun `should return paged list of businesses`() {
        // Given
        val page = 0
        val pageSize = 10
        val totalItems = businessRepository.count()
        val expectedPages = (totalItems + pageSize - 1) / pageSize

        // When
        val result = adminBusinessService.findAllPaged(page)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.totalItems).isEqualTo(businessRepository.count())
        assertThat(result.totalPages).isEqualTo(expectedPages)
    }

    @Test
    @DisplayName("사업자 페이지 조회 - 존재하지 않는 페이지 조회 시 예외 발생")
    fun `should throw exception when invalid page requested`() {
        // Given
        val pageSize = 10
        val totalElements = businessRepository.count()
        val invalidPage = ((totalElements - 1) / pageSize).toInt() + 1

        // When
        val exception = assertThrows<ServiceException> {
            adminBusinessService.findAllPaged(invalidPage)
        }

        // Then
        assertThat(exception.resultCode).isEqualTo(ErrorCode.PAGE_NOT_FOUND)
    }

    @Test
    @DisplayName("사업자 조회 - 정상적으로 조회될 때")
    fun `should find business by id`() {
        // Given
        val expectedResult = AdminBusinessResponse.Detail.from(
            businessRepository.findById(testId)
                .orElseThrow(ErrorCode.BUSINESS_NOT_FOUND::throwServiceException)
        )

        // When
        val result = adminBusinessService.findById(testId)

        // Then
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("사업자 조회 - 존재하지 않는 사업자 조회 시 예외 발생")
    fun `should throw exception when business not found`() {
        // Given
        val invalidBusinessId = businessRepository.count() + 1

        // When
        val exception = assertThrows<ServiceException> {
            adminBusinessService.findById(invalidBusinessId)
        }

        // Then
        assertThat(exception.resultCode).isEqualTo(ErrorCode.BUSINESS_NOT_FOUND)
    }

    @Test
    @DisplayName("사업자 승인")
    fun `should approve business and update member role`() {
        // Given
        val adminBusinessRequest = AdminBusinessRequest(BusinessApprovalStatus.APPROVED)

        // When
        val result = adminBusinessService.approve(testId, adminBusinessRequest)

        // Then: 메모리 상의 데이터 검증
        assertThat(result.approvalStatus).isEqualTo(BusinessApprovalStatus.APPROVED)

        // Then: DB의 데이터 검증
        val savedBusiness = businessRepository.findById(testId).orElseThrow(
            ErrorCode.BUSINESS_NOT_FOUND::throwServiceException
        )
        assertThat(savedBusiness.approvalStatus).isEqualTo(BusinessApprovalStatus.APPROVED)
    }
}
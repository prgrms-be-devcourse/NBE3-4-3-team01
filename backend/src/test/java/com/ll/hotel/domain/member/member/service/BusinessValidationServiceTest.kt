package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.exceptions.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.assertj.core.api.Assertions.assertThat
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
class BusinessValidationServiceTest(
    private val businessValidationService: BusinessValidationService
) {

    private val logger: Logger = LoggerFactory.getLogger(
        BusinessValidationServiceTest::class.java)

    @Test
    @DisplayName("유효하지 않은 사업자 - 02")
    fun `should throw ServiceException for invalid business`() {
        // Given
        val registrationInfo = BusinessRequest.RegistrationInfo(
            businessRegistrationNumber = "1234567890",
            startDate =  LocalDate.now(),
            ownerName = "홍길동"
        )

        // When
        val exception = assertThrows<ServiceException> {
            businessValidationService.validateBusiness(registrationInfo)
        }

        logger.error("예외 발생: ${exception.resultCode} - ${exception.message}", exception)

        // Then
        assertThat(exception.resultCode)
            .isIn(
                ErrorCode.INVALID_BUSINESS_INFO,
                ErrorCode.EXTERNAL_API_UNEXPECTED_RESPONSE,
                ErrorCode.EXTERNAL_API_COMMUNICATION_ERROR
            )
    }
}
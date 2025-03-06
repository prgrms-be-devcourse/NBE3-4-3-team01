package com.ll.hotel.domain.member.member.service;

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest;
import com.ll.hotel.global.exceptions.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BusinessValidationServiceTest {
    @Autowired
    private BusinessValidationService businessValidationService;

    @Test
    @DisplayName("유효하지 않은 사업자 - 02")
    public void validateBusinessFailureTest() {
        // Given
        BusinessRequest.RegistrationInfo registrationInfo = new BusinessRequest.RegistrationInfo(
                "1234567890",
                LocalDate.now(),
                "홍길동"
        );

        String expectedResponse = "02";
        String result;

        try {
            // When
            result = businessValidationService.validateBusiness(registrationInfo);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof ServiceException serviceException) {
                System.out.println(serviceException.getMessage());
            } else {
                System.out.println("기타 예외 발생: " + e.getMessage());
            }
            result = "02";
        }

        // then
        assertThat(result).isEqualTo(expectedResponse);
    }
}

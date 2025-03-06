package com.ll.hotel.domain.member.member.controller;

import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.service.BusinessService;
import com.ll.hotel.domain.member.member.service.BusinessValidationService;
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;
import com.ll.hotel.global.exceptions.handler.GlobalExceptionHandler;
import com.ll.hotel.global.request.Rq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class BusinessControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private BusinessController businessController;

    @Mock
    private Rq rq;

    @Mock
    private BusinessValidationService businessValidationService;

    @Mock
    private BusinessService businessService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(businessController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
    @Test
    @DisplayName("유효한 사업자 등록")
    void registerBusinessTest() throws Exception {
        // Given
        Member mockMember = Member.builder()
                .memberName("홍길동")
                .role(Role.USER)
                .build();

        Business mockBusiness = Business.builder()
                .businessRegistrationNumber("1234567890")
                .ownerName("홍길동")
                .startDate(LocalDate.of(2020, 1, 1))
                .approvalStatus(BusinessApprovalStatus.APPROVED)
                .build();

        // Mock 동작 설정
        when(rq.getActor()).thenReturn(mockMember);
        when(businessValidationService.validateBusiness(any())).thenReturn("01");
        when(businessService.register(any(), any(), any())).thenReturn(mockBusiness);

        String requestBody = """
                {
                    "businessRegistrationNumber": "1234567890",
                    "startDate": "2020-01-01",
                    "ownerName": "홍길동"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/businesses/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(jsonPath("$.resultCode").value(HttpStatus.CREATED.name()));
    }
}

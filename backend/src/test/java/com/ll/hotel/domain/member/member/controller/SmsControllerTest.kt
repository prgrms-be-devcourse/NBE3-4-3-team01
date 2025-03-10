package com.ll.hotel.domain.member.member.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ll.hotel.domain.member.member.dto.SmsRequestDto
import com.ll.hotel.domain.member.member.dto.SmsResponseDto
import com.ll.hotel.domain.member.member.dto.SmsVerifyRequestDto
import com.ll.hotel.domain.member.member.service.SmsService
import com.ll.hotel.global.exceptions.ServiceException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SmsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Autowired
    private lateinit var smsController: SmsController

    @Mock
    private lateinit var smsService: SmsService

    private val testPhoneNumber = "010-1234-5678"
    private val testCode = "123456"
    
    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        ReflectionTestUtils.setField(smsController, "smsService", smsService)
    }

    @Test
    @DisplayName("SMS 발송 성공 테스트")
    fun sendSmsSuccess() {
        // Given
        val requestDto = SmsRequestDto(testPhoneNumber)
        val responseDto = SmsResponseDto(true, "인증번호가 발송되었습니다.")
        
        `when`(smsService.sendSms(testPhoneNumber)).thenReturn(responseDto)

        // When & Then
        mockMvc.perform(
            post("/api/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name))
            .andExpect(jsonPath("$.msg").value("OK"))
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.message").value("인증번호가 발송되었습니다."))
    }

    @Test
    @DisplayName("SMS 발송 실패 테스트 - 전화번호 제한 초과")
    fun sendSmsFail_PhoneNumberLimitExceeded() {
        // Given
        val requestDto = SmsRequestDto(testPhoneNumber)
        val responseDto = SmsResponseDto(false, "해당 휴대폰 번호로는 더 이상 가입할 수 없습니다.")
        
        `when`(smsService.sendSms(testPhoneNumber)).thenReturn(responseDto)

        // When & Then
        mockMvc.perform(
            post("/api/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name))
            .andExpect(jsonPath("$.msg").value("OK"))
            .andExpect(jsonPath("$.data.success").value(false))
            .andExpect(jsonPath("$.data.message").value("해당 휴대폰 번호로는 더 이상 가입할 수 없습니다."))
    }

    @Test
    @DisplayName("SMS 인증 성공 테스트")
    fun verifySmsSuccess() {
        // Given
        val requestDto = SmsVerifyRequestDto(testPhoneNumber, testCode)
        val responseDto = SmsResponseDto(true, "인증이 완료되었습니다.")
        
        `when`(smsService.verifySms(testPhoneNumber, testCode)).thenReturn(responseDto)

        // When & Then
        mockMvc.perform(
            post("/api/sms/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name))
            .andExpect(jsonPath("$.msg").value("OK"))
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.message").value("인증이 완료되었습니다."))
    }

    @Test
    @DisplayName("SMS 인증 실패 테스트 - 코드 불일치")
    fun verifySmsFail_CodeMismatch() {
        // Given
        val requestDto = SmsVerifyRequestDto(testPhoneNumber, "wrong-code")
        val responseDto = SmsResponseDto(false, "인증번호가 일치하지 않거나 만료되었습니다.")
        
        `when`(smsService.verifySms(testPhoneNumber, "wrong-code")).thenReturn(responseDto)

        // When & Then
        mockMvc.perform(
            post("/api/sms/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name))
            .andExpect(jsonPath("$.msg").value("OK"))
            .andExpect(jsonPath("$.data.success").value(false))
            .andExpect(jsonPath("$.data.message").value("인증번호가 일치하지 않거나 만료되었습니다."))
    }

    @Test
    @DisplayName("SMS 발송 예외 처리 테스트")
    fun sendSmsException() {
        // Given
        val requestDto = SmsRequestDto(testPhoneNumber)
        
        `when`(smsService.sendSms(testPhoneNumber)).thenThrow(ServiceException(HttpStatus.BAD_REQUEST, "SMS 발송 중 오류가 발생했습니다."))

        // When & Then
        mockMvc.perform(
            post("/api/sms/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("SMS 발송 중 오류가 발생했습니다."))
    }

    @Test
    @DisplayName("SMS 인증 예외 처리 테스트")
    fun verifySmsException() {
        // Given
        val requestDto = SmsVerifyRequestDto(testPhoneNumber, testCode)
        
        `when`(smsService.verifySms(testPhoneNumber, testCode)).thenThrow(ServiceException(HttpStatus.BAD_REQUEST, "SMS 인증 중 오류가 발생했습니다."))

        // When & Then
        mockMvc.perform(
            post("/api/sms/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").value("SMS 인증 중 오류가 발생했습니다."))
    }
} 
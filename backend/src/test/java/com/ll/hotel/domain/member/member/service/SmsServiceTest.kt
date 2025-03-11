package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.repository.SmsCertificationRepository
import com.ll.hotel.global.exceptions.ServiceException
import com.ll.hotel.standard.util.SmsCertificationUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SmsServiceTest {

    @Mock
    private lateinit var smsCertificationUtil: SmsCertificationUtil

    @Mock
    private lateinit var smsCertificationDao: SmsCertificationRepository

    @Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var smsService: SmsServiceImpl

    private val testPhoneNumber = "010-1234-5678"
    private val testCertificationCode = "123456"

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        smsService = SmsServiceImpl(smsCertificationUtil, smsCertificationDao, memberRepository)
        
        lenient().`when`(smsCertificationUtil.isProductionMode()).thenReturn(false)
    }

    @Test
    @DisplayName("SMS 발송 성공 테스트")
    fun sendSmsSuccess() {
        // Given
        `when`(memberRepository.countByMemberPhoneNumber(testPhoneNumber)).thenReturn(0)
        doReturn(null).`when`(smsCertificationUtil).sendSMS(anyString(), anyString())

        // When
        val result = smsService.sendSms(testPhoneNumber)

        // Then
        assertTrue(result.success)
        assertEquals("인증번호가 발송되었습니다.", result.message)
        verify(smsCertificationDao, times(1)).createSmsCertification(anyString(), anyString())
    }

    @Test
    @DisplayName("휴대폰 번호 제한 초과 테스트")
    fun sendSmsPhoneNumberLimitExceeded() {
        // Given
        `when`(memberRepository.countByMemberPhoneNumber(testPhoneNumber)).thenReturn(1)

        // When & Then
        val exception = org.junit.jupiter.api.Assertions.assertThrows(ServiceException::class.java) {
            smsService.sendSms(testPhoneNumber)
        }
        
        assertEquals("해당 휴대폰 번호로는 더 이상 가입할 수 없습니다.", exception.msg)
        verify(smsCertificationUtil, never()).sendSMS(anyString(), anyString())
        verify(smsCertificationDao, never()).createSmsCertification(anyString(), anyString())
    }

    @Test
    @DisplayName("SMS 발송 실패 테스트")
    fun sendSmsFail() {
        // Given
        `when`(memberRepository.countByMemberPhoneNumber(testPhoneNumber)).thenReturn(0)
        doThrow(RuntimeException("SMS 발송 실패")).`when`(smsCertificationUtil).sendSMS(anyString(), anyString())

        // When & Then
        val exception = org.junit.jupiter.api.Assertions.assertThrows(ServiceException::class.java) {
            smsService.sendSms(testPhoneNumber)
        }
        
        assertEquals("SMS 발송 중 오류가 발생했습니다.", exception.msg)
    }

    @Test
    @DisplayName("인증번호 확인 성공 테스트")
    fun verifySuccess() {
        // Given
        `when`(smsCertificationDao.getSmsCertification(testPhoneNumber)).thenReturn(testCertificationCode)

        // When
        val result = smsService.verifySms(testPhoneNumber, testCertificationCode)

        // Then
        assertTrue(result.success)
        assertEquals("인증이 완료되었습니다.", result.message)
        verify(smsCertificationDao, times(1)).removeSmsCertification(testPhoneNumber)
    }

    @Test
    @DisplayName("인증번호 불일치 테스트")
    fun verifyCodeMismatch() {
        // Given
        `when`(smsCertificationDao.getSmsCertification(testPhoneNumber)).thenReturn(testCertificationCode)

        // When & Then
        val exception = org.junit.jupiter.api.Assertions.assertThrows(ServiceException::class.java) {
            smsService.verifySms(testPhoneNumber, "654321") // 다른 인증번호
        }
        
        assertEquals("인증번호가 일치하지 않거나 만료되었습니다.", exception.msg)
        verify(smsCertificationDao, never()).removeSmsCertification(anyString())
    }

    @Test
    @DisplayName("인증번호 만료 테스트")
    fun verifyCodeExpired() {
        // Given
        `when`(smsCertificationDao.getSmsCertification(testPhoneNumber)).thenReturn(null)

        // When & Then
        val exception = org.junit.jupiter.api.Assertions.assertThrows(ServiceException::class.java) {
            smsService.verifySms(testPhoneNumber, testCertificationCode)
        }
        
        assertEquals("인증번호가 일치하지 않거나 만료되었습니다.", exception.msg)
        verify(smsCertificationDao, never()).removeSmsCertification(anyString())
    }

    @Test
    @DisplayName("휴대폰 번호 제한 확인 테스트")
    fun checkPhoneNumberLimit() {
        // Given
        `when`(memberRepository.countByMemberPhoneNumber(testPhoneNumber)).thenReturn(0)

        // When
        val result = smsService.checkPhoneNumberLimit(testPhoneNumber)

        // Then
        assertTrue(result)
    }

    @Test
    @DisplayName("휴대폰 번호 제한 초과 확인 테스트")
    fun checkPhoneNumberLimitExceeded() {
        // Given
        `when`(memberRepository.countByMemberPhoneNumber(testPhoneNumber)).thenReturn(1)

        // When
        val result = smsService.checkPhoneNumberLimit(testPhoneNumber)

        // Then
        assertFalse(result)
    }
} 
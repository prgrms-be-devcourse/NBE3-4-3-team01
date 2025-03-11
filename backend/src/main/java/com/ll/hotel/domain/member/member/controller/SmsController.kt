package com.ll.hotel.domain.member.member.controller

import com.ll.hotel.domain.member.member.dto.SmsRequestDto
import com.ll.hotel.domain.member.member.dto.SmsResponseDto
import com.ll.hotel.domain.member.member.dto.SmsVerifyRequestDto
import com.ll.hotel.domain.member.member.service.SmsService
import com.ll.hotel.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sms")
@Tag(name = "SmsController", description = "SMS 인증 API")
class SmsController(private val smsService: SmsService) {
    
    private val logger = LoggerFactory.getLogger(SmsController::class.java)
    
    @PostMapping("/send")
    @Operation(summary = "SMS 인증번호 발송", description = "입력한 휴대폰 번호로 인증번호를 발송합니다.")
    fun sendSms(@RequestBody request: SmsRequestDto): RsData<SmsResponseDto> {
        logger.debug("SMS 발송 요청 컨트롤러: request={}", request)
        val result = smsService.sendSms(request.phoneNumber)
        logger.debug("SMS 발송 결과: {}", result)
        return RsData.success(HttpStatus.OK, result)
    }
    
    @PostMapping("/verify")
    @Operation(summary = "SMS 인증번호 확인", description = "발송된 인증번호의 유효성을 검증합니다.")
    fun verifySms(@RequestBody request: SmsVerifyRequestDto): RsData<SmsResponseDto> {
        logger.debug("SMS 인증 요청 컨트롤러: request={}", request)
        val result = smsService.verifySms(request.phoneNumber, request.code)
        logger.debug("SMS 인증 결과: {}", result)
        return RsData.success(HttpStatus.OK, result)
    }
} 
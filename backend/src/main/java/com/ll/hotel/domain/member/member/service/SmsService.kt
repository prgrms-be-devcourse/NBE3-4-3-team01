package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.dto.SmsResponseDto

interface SmsService {
    fun sendSms(phoneNumber: String): SmsResponseDto
    fun verifySms(phoneNumber: String, code: String): SmsResponseDto
    fun checkPhoneNumberLimit(phoneNumber: String, limit: Int = 1): Boolean
} 
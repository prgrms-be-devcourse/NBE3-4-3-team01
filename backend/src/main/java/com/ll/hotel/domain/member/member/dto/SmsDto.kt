package com.ll.hotel.domain.member.member.dto

data class SmsRequestDto(
    val phoneNumber: String
) {
    override fun toString(): String {
        return "SmsRequestDto(phoneNumber='$phoneNumber')"
    }
}

data class SmsVerifyRequestDto(
    val phoneNumber: String,
    val code: String
) {
    override fun toString(): String {
        return "SmsVerifyRequestDto(phoneNumber='$phoneNumber', code='$code')"
    }
}

data class SmsResponseDto(
    val success: Boolean,
    val message: String
) {
    override fun toString(): String {
        return "SmsResponseDto(success=$success, message='$message')"
    }
} 
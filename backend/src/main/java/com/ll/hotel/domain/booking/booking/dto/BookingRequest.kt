package com.ll.hotel.domain.booking.booking.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class BookingRequest(
    @field:NotNull(message = "객실 정보는 필수입니다.")
    @field:Min(value = 1, message = "유효한 객실 ID를 입력해주세요.")
    val roomId: Long,

    @field:NotNull(message = "호텔 정보는 필수입니다.")
    @field:Min(value = 1, message = "유효한 호텔 ID를 입력해주세요.")
    val hotelId: Long,

    @field:NotNull(message = "체크인 일자는 필수입니다.")
    val checkInDate: LocalDate,

    @field:NotNull(message = "체크아웃 일자는 필수입니다.")
    val checkOutDate: LocalDate,

    @field:NotNull(message = "거래 Uid는 필수입니다.")
    @field:Size(min = 10, max = 10, message = "거래 Uid는 10자리여야 합니다.")
    val merchantUid: String,

    @field:NotNull(message = "거래 금액은 필수입니다.")
    @field:Min(value = 0, message = "거래 금액은 0 이상이어야 합니다.")
    val amount: Int,

    @field:NotNull(message = "거래 일자는 필수입니다.")
    @field:Min(value = 1, message = "유효한 거래 일자를 입력해주세요.")
    val paidAtTimestamp: Long
)
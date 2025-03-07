package com.ll.hotel.global.validation

import com.ll.hotel.global.exceptions.ErrorCode
import java.time.LocalDate

object GlobalValidation {
    @JvmStatic
    fun checkPageSize(pageSize: Int) {
        if (pageSize < 1 || pageSize > 100) {
            ErrorCode.PAGE_SIZE_LIMIT_EXCEEDED.throwServiceException()
        }
    }

    @JvmStatic
    fun checkCheckInAndOutDate(checkInDate: LocalDate, checkOutDate: LocalDate) {
        if (checkInDate.isAfter(checkOutDate)) {
            ErrorCode.INVALID_CHECK_IN_OUT_DATE.throwServiceException()
        }
    }
}
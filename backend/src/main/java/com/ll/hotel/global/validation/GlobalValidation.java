package com.ll.hotel.global.validation;

import com.ll.hotel.global.exceptions.ErrorCode;
import java.time.LocalDate;

public class GlobalValidation {
    public static void checkPageSize(int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            ErrorCode.PAGE_SIZE_LIMIT_EXCEEDED.throwServiceException();
        }
    }

    public static void checkCheckInAndOutDate(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate.isAfter(checkOutDate)) {
            ErrorCode.INVALID_CHECK_IN_OUT_DATE.throwServiceException();
        }
    }
}

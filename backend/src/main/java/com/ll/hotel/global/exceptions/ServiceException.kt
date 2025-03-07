package com.ll.hotel.global.exceptions

import org.springframework.http.HttpStatus

class ServiceException(
    val resultCode: HttpStatus,
    val msg: String,
    cause: Throwable? = null
) : RuntimeException("$resultCode : $msg", cause)
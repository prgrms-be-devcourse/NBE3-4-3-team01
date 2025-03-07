package com.ll.hotel.global.exceptions

import org.springframework.http.HttpStatus

class CustomS3Exception(
    val resultCode: HttpStatus,
    val msg: String,
    cause: Throwable? = null
) : RuntimeException("$resultCode:$msg", cause)
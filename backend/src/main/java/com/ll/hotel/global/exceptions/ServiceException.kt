package com.ll.hotel.global.exceptions

import org.springframework.http.HttpStatus

class ServiceException : RuntimeException {
    val resultCode: HttpStatus
    val msg: String

    constructor(resultCode: HttpStatus, msg: String) : super("$resultCode : $msg") {
        this.resultCode = resultCode
        this.msg = msg
    }

    constructor(resultCode: HttpStatus, msg: String, cause: Throwable) : super("$resultCode : $msg", cause) {
        this.resultCode = resultCode
        this.msg = msg
    }
}
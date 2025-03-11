package com.ll.hotel.global.response

import org.springframework.http.HttpStatus

class RsData<T>(
    val resultCode: HttpStatus,
    val msg: String,
    val data: T
) {
    val isSuccess: Boolean get() = !resultCode.isError

    companion object {
        @JvmStatic  // 모두 코틀린화 하면 제거
        fun <T> success(resultCode: HttpStatus, data: T): RsData<T> {
            return RsData(resultCode, "OK", data)
        }
    }
}
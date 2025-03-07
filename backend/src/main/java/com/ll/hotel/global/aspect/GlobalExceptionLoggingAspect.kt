package com.ll.hotel.global.aspect

import com.ll.hotel.standard.util.LogUtil
import com.ll.hotel.standard.util.logger
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class GlobalExceptionLoggingAspect {
    val log = logger()

    @AfterThrowing(pointcut = "@within(org.springframework.stereotype.Service)", throwing = "ex")
    fun logGlobalException(joinPoint: JoinPoint, ex: Throwable) {
        LogUtil.logError(logger(), joinPoint, ex)
    }
}
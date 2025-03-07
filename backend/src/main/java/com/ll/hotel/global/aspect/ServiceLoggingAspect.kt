package com.ll.hotel.global.aspect

import com.ll.hotel.standard.util.LogUtil
import com.ll.hotel.standard.util.logger
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component

@Aspect
@Component
class ServiceLoggingAspect {
    private val log = logger()

    @Before("@within(org.springframework.stereotype.Service)")
    fun logService(joinPoint: JoinPoint): Any {
        val className = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name

        LogUtil.logServiceRequest(log, className, methodName)

        return joinPoint
    }
}

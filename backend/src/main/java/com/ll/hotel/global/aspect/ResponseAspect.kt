package com.ll.hotel.global.aspect

import com.ll.hotel.global.app.AppConfig
import com.ll.hotel.global.response.RsData
import com.ll.hotel.standard.util.LogUtil
import com.ll.hotel.standard.util.logger
import jakarta.servlet.http.HttpServletResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class ResponseAspect(private val response: HttpServletResponse) {
    private val log = logger()

    @Around(
        """
            (
                within
                (
                    @org.springframework.web.bind.annotation.RestController *
                )
                &&
                (
                    @annotation(org.springframework.web.bind.annotation.GetMapping)
                    ||
                    @annotation(org.springframework.web.bind.annotation.PostMapping)
                    ||
                    @annotation(org.springframework.web.bind.annotation.PutMapping)
                    ||
                    @annotation(org.springframework.web.bind.annotation.DeleteMapping)
                    ||
                    @annotation(org.springframework.web.bind.annotation.RequestMapping)
                )
            )
            ||
            @annotation(org.springframework.web.bind.annotation.ResponseBody)
            """
    )
    @Throws(Throwable::class)
    fun handleResponse(joinPoint: ProceedingJoinPoint): Any {
        val className = joinPoint.signature.declaringType.simpleName
        val methodName = joinPoint.signature.name

        LogUtil.logControllerRequest(log, className, methodName)

        val proceed = joinPoint.proceed()

        if (proceed is RsData<*>) {
            LogUtil.logControllerResponse(log, className, methodName, proceed)

            response.status = proceed.resultCode.value()
        }

        return proceed
    }
}
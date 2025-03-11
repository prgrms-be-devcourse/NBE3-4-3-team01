package com.ll.hotel.global.aspect

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.standard.util.LogUtil
import com.ll.hotel.standard.util.logger
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component

@Aspect
@Component
class BusinessCheckAspect {
    val log = logger()

    @Before("@annotation(com.ll.hotel.global.annotation.BusinessOnly)")
    fun checkBusiness(joinPoint: JoinPoint) {
        try {
            joinPoint.args.forEach {
                if (it is Member) {
                    it.checkBusiness()
                }
            }
        } catch (ex: Throwable) {
            LogUtil.logError(logger(), joinPoint, ex)
            throw ex
        }
    }
}
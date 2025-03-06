package com.ll.hotel.global.aspect;

import com.ll.hotel.domain.member.member.entity.Member;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BusinessCheckAspect {

    @Before("@annotation(com.ll.hotel.global.annotation.BusinessOnly)")
    public void checkBusiness(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Member member) {
                member.checkBusiness();
                return;
            }
        }
    }
}

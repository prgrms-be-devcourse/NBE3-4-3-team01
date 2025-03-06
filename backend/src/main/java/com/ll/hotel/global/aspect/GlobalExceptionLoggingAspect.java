package com.ll.hotel.global.aspect;

import com.ll.hotel.global.exceptions.CustomS3Exception;
import com.ll.hotel.global.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionLoggingAspect {
    @AfterThrowing(pointcut = "@within(org.springframework.stereotype.Service)", throwing = "ex")
    public void logGlobalException(JoinPoint joinPoint, Throwable ex) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String status;
        String msg;

        switch (ex) {
            case ServiceException exception -> {
                status = exception.getResultCode().toString();
                msg = exception.getMsg();
            }
            case CustomS3Exception exception -> {
                status = exception.getResultCode().toString();
                msg = exception.getMsg();
            }
            case MethodArgumentNotValidException exception -> {
                status = HttpStatus.BAD_REQUEST.toString();
                msg = exception.getMessage();
            }
            default -> {
                status = "UNKNOWN";
                msg = ex.getMessage();
            }
        }

        log.error("ERROR = [{}.{}], status: [{}], message: [{}]",
                className, methodName, status, msg
        );
    }
}
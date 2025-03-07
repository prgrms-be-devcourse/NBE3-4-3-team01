package com.ll.hotel.global.exceptions.handler

import com.ll.hotel.global.exceptions.CustomS3Exception
import com.ll.hotel.global.exceptions.ServiceException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.stream.Collectors

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException::class)
    fun handle(ex: ServiceException): ResponseEntity<String> {
        return ResponseEntity
            .status(ex.resultCode)
            .body(ex.msg)
    }

    @ExceptionHandler(CustomS3Exception::class)
    fun handle(ex: CustomS3Exception): ResponseEntity<String> {
        return ResponseEntity
            .status(ex.resultCode)
            .body(ex.msg)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<String> {
        val fieldErrors = ex.bindingResult.fieldErrors
        val errorMessages = fieldErrors.stream()
            .map { "${it.field}: ${it.defaultMessage}" }
            .collect(Collectors.joining("\n"))

        return ResponseEntity
            .badRequest()
            .body(errorMessages)
    }
}
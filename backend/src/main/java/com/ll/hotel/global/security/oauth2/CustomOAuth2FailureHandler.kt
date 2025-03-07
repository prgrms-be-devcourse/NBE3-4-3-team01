package com.ll.hotel.global.security.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

import java.io.IOException

import com.ll.hotel.global.exceptions.ErrorCode.OAUTH_LOGIN_FAILED

@Component
class CustomOAuth2FailureHandler(
    private val objectMapper: ObjectMapper
) : AuthenticationFailureHandler {
    
    private val log = LoggerFactory.getLogger(CustomOAuth2FailureHandler::class.java)

    @Throws(IOException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest, 
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.contentType = "application/json;charset=UTF-8"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write(objectMapper.writeValueAsString(OAUTH_LOGIN_FAILED))
    }
} 
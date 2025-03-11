package com.ll.hotel.global.security.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import com.ll.hotel.global.exceptions.ErrorCode.OAUTH_LOGIN_FAILED
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomOAuth2FailureHandler(
    private val objectMapper: ObjectMapper
) : AuthenticationFailureHandler {

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
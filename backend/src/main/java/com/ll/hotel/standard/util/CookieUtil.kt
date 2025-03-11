package com.ll.hotel.standard.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie

object CookieUtil {
    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        val cookies = request.cookies ?: return null
        return cookies.firstOrNull { it.name == name }
    }

    fun addCookie(
        response: HttpServletResponse, 
        name: String, 
        value: String, 
        maxAge: Int, 
        isHttpOnly: Boolean = true, 
        isSecure: Boolean = false, 
        sameSite: String = "Lax"
    ) {
        val cookie = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(isHttpOnly)
            .maxAge(maxAge.toLong())
            .secure(isSecure)
            .sameSite(sameSite)
            .build()
            
        response.addHeader("Set-Cookie", cookie.toString())
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies ?: return
        cookies.filter { it.name == name }
            .forEach { _ ->
                val cookie = ResponseCookie.from(name, "")
                    .path("/")
                    .maxAge(0)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .build()
                    
                response.addHeader("Set-Cookie", cookie.toString())
            }
    }
}
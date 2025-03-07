package com.ll.hotel.global.jwt.exception

import com.ll.hotel.global.exceptions.ServiceException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import java.io.IOException

@Component
@Order(0)
class JwtExceptionFilter : OncePerRequestFilter() {

/*   filter 는 DispatcherServlet 앞단에서 동작하기 때문에, filter 에서 발생한 예외는
     DispatcherServlet 까지 전달되지 않아서 GlobalExceptionHandler 에서 처리할 수 없음 */

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: ServiceException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            
            response.writer.write(String.format(
                "{\"resultCode\": \"%d-1\", \"msg\": \"%s\", \"data\": null}", 
                HttpServletResponse.SC_UNAUTHORIZED,
                e.message
            ))
        }
    }
} 
package com.ll.hotel.global.security

import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.global.jwt.JwtAuthFilter
import com.ll.hotel.global.jwt.exception.JwtExceptionFilter
import com.ll.hotel.global.security.cors.CorsProperties
import com.ll.hotel.global.security.oauth2.CustomOAuth2AuthenticationSuccessHandler
import com.ll.hotel.global.security.oauth2.CustomOAuth2AuthorizationRequestRepository
import com.ll.hotel.global.security.oauth2.CustomOAuth2FailureHandler
import com.ll.hotel.global.security.oauth2.CustomOAuth2UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2AuthenticationSuccessHandler: CustomOAuth2AuthenticationSuccessHandler,
    private val oAuth2AuthenticationFailureHandler: CustomOAuth2FailureHandler,
    private val memberService: MemberService,
    private val memberRepository: MemberRepository,
    private val customOAuth2AuthorizationRequestRepository: CustomOAuth2AuthorizationRequestRepository,
    private val corsProperties: CorsProperties
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            .authorizeHttpRequests { authorizeHttpRequests ->
                authorizeHttpRequests
                    .requestMatchers(
                        "/",
                        "/error",
                        "/favicon.ico",
                        "/h2-console/**",
                        "/api/auth/**",
                        "/api/users/join",
                        "/api/users/login",
                        "/api/users/refresh",
                        "/api/hotels/**",
                        "/api/bookings/**",
                        "/api/favorites/**",
                        "/api/reviews/**",
                        "/oauth2/authorization/**",
                        "/login/oauth2/code/**",
                        "/api/*/oauth2/callback"
                    ).permitAll()

                    // 관리자 전용
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // 사업자 전용
                    .requestMatchers("/api/businesses/register").hasAnyRole("USER", "BUSINESS")
                    .requestMatchers("/api/businesses/**").hasRole("BUSINESS")

                    // 애플리케이션에만 나머지 인증 요구
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            }
            .exceptionHandling { exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint { request, response, authException ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = HttpServletResponse.SC_UNAUTHORIZED
                        response.writer.write(
                            String.format(
                                "{\"resultCode\": \"%d-1\", \"msg\": \"%s\", \"data\": null}",
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "사용자 인증정보가 올바르지 않습니다."
                            )
                        )
                    }
                    .accessDeniedHandler { request, response, accessDeniedException ->
                        response.contentType = "application/json;charset=UTF-8"
                        response.status = HttpServletResponse.SC_FORBIDDEN
                        response.writer.write(
                            String.format(
                                "{\"resultCode\": \"%d-1\", \"msg\": \"%s\", \"data\": null}",
                                HttpServletResponse.SC_FORBIDDEN,
                                "접근 권한이 없습니다."
                            )
                        )
                    }
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .oauth2Login { oauth2Login ->
                oauth2Login
                    .authorizationEndpoint { endpoint ->
                        endpoint.authorizationRequestRepository(customOAuth2AuthorizationRequestRepository)
                    }
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(customOAuth2UserService)
                    }
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            }
            .addFilterBefore(JwtExceptionFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(JwtAuthFilter(memberService, memberRepository), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = corsProperties.allowedOrigins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
} 
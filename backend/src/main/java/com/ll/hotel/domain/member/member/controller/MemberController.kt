package com.ll.hotel.domain.member.member.controller

import com.ll.hotel.domain.member.member.dto.JoinRequest
import com.ll.hotel.domain.member.member.dto.MemberDTO
import com.ll.hotel.domain.member.member.dto.MemberResponse
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.global.exceptions.ErrorCode.EMAIL_ALREADY_EXISTS
import com.ll.hotel.global.exceptions.ErrorCode.REFRESH_TOKEN_NOT_FOUND
import com.ll.hotel.global.exceptions.ServiceException
import com.ll.hotel.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/users")
@Tag(name = "MemberController", description = "회원 관리 API")
class MemberController(
    private val memberService: MemberService
) {
    private val log = LoggerFactory.getLogger(MemberController::class.java)

    @PostMapping("/join")
    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    fun join(
        @RequestBody @Valid joinRequest: JoinRequest, 
        response: HttpServletResponse,
        bindingResult: BindingResult
    ): RsData<MemberResponse> {
        if (bindingResult.hasErrors()) {
            val errorMessage = bindingResult.fieldErrors
                .stream()
                .map { error -> "${error.field}: ${error.defaultMessage}" }
                .collect(Collectors.joining(", "))
            
            throw EMAIL_ALREADY_EXISTS.throwServiceException()
        }
        
        try {
            val member = memberService.join(joinRequest)
            
            // OAuth2 회원가입인 경우 자동 로그인 처리
            if (joinRequest.provider.isNotBlank() && joinRequest.oauthId.isNotBlank()) {
                memberService.oAuth2Login(member, response)
            }
            
            val memberResponse = MemberResponse(
                MemberDTO.from(member),
                "회원가입이 완료되었습니다. 이메일: " + 
                member.memberEmail
            )
            return RsData.success(HttpStatus.OK, memberResponse)
        } catch (e: ServiceException) {
            throw EMAIL_ALREADY_EXISTS.throwServiceException(e)
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리합니다.")
    fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        memberService.logout(request, response)
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    fun refresh(request: HttpServletRequest): RsData<String> {
        var refreshToken: String? = null
        val cookies = request.cookies
        
        if (cookies != null) {
            for (cookie in cookies) {
                if ("refresh_token" == cookie.name) {
                    refreshToken = cookie.value
                    break
                }
            }
        }
        
        // 헤더에서도 확인
        if (refreshToken == null) {
            val authHeader = request.getHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7)
            }
        }
        
        if (refreshToken == null) {
            throw REFRESH_TOKEN_NOT_FOUND.throwServiceException()
        }
        
        return memberService.refreshAccessToken(refreshToken)
    }
} 
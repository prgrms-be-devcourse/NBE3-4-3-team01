package com.ll.hotel.domain.member.member.controller;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ll.hotel.domain.member.member.dto.JoinRequest;
import com.ll.hotel.domain.member.member.dto.MemberDTO;
import com.ll.hotel.domain.member.member.dto.MemberResponse;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.service.MemberService;
import static com.ll.hotel.global.exceptions.ErrorCode.EMAIL_ALREADY_EXISTS;
import static com.ll.hotel.global.exceptions.ErrorCode.REFRESH_TOKEN_NOT_FOUND;
import com.ll.hotel.global.exceptions.ServiceException;
import com.ll.hotel.global.response.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "MemberController", description = "회원 관리 API")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/join")
    @Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
    public RsData<MemberResponse> join(@RequestBody @Valid JoinRequest joinRequest, 
                                     HttpServletResponse response,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

            EMAIL_ALREADY_EXISTS.throwServiceException();
        }
        
        try {
            Member member = memberService.join(joinRequest);
            
            // OAuth2 회원가입인 경우 자동 로그인 처리
            if (joinRequest.provider() != null && joinRequest.oauthId() != null) {
                memberService.oAuth2Login(member, response);
            }
            
            MemberResponse memberResponse = new MemberResponse(
                MemberDTO.from(member),
                member.getMemberEmail()
            );
            return RsData.success(HttpStatus.OK, memberResponse);
        } catch (ServiceException e) {
            throw EMAIL_ALREADY_EXISTS.throwServiceException(e);
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리합니다.")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        memberService.logout(request, response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    public RsData<String> refresh(HttpServletRequest request) {
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        
        if (refreshToken == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refreshToken = authHeader.substring(7);
            }
        }
        
        if (refreshToken == null) {
            REFRESH_TOKEN_NOT_FOUND.throwServiceException();
        }
        
        return memberService.refreshAccessToken(refreshToken);
    }
}
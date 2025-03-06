package com.ll.hotel.domain.member.member.service;


import com.ll.hotel.global.jwt.dto.GeneratedToken;
import com.ll.hotel.global.jwt.dto.JwtProperties;
import com.ll.hotel.standard.util.Ut;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthTokenService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    public GeneratedToken generateToken(String email, String role) {
        String accessToken = genAccessToken(email, role);
        String refreshToken = refreshTokenService.generateRefreshToken(email);

        refreshTokenService.saveTokenInfo(email, refreshToken, accessToken);
        return new GeneratedToken(accessToken, refreshToken);
    }

    String genAccessToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("role", role);
        claims.put("type", "access");

        return Ut.jwt.toString(jwtProperties, claims);
    }

    boolean verifyToken(String token) {
        try {
            Claims claims = Ut.jwt.getClaims(jwtProperties, token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    String getEmail(String token) {
        return Ut.jwt.getClaims(jwtProperties, token).getSubject();
    }
}
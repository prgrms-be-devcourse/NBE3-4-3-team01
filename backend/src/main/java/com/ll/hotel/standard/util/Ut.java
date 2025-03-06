package com.ll.hotel.standard.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.hotel.global.app.AppConfig;
import com.ll.hotel.global.jwt.dto.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class Ut {

    private final SecretKey secretKey;

    public Ut(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public static class str {
        public static boolean isBlank(String str) {
            return str == null || str.trim().isEmpty();
        }
    }

    public static class json {
        private static final ObjectMapper om = AppConfig.getObjectMapper();

        @SneakyThrows
        public static String toString(Object obj) {
            return om.writeValueAsString(obj);
        }

        @SneakyThrows
        public static Map<String, Object> toMap(String jsonStr) {
            return om.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        }
    }

    public static class jwt {
        public static String toString(JwtProperties jwtProperties, Map<String, Object> claims) {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
            Date now = new Date();

            String tokenType = (String) claims.getOrDefault("type", "access");
            long expiration = tokenType.equals("refresh")
                    ? jwtProperties.getRefreshTokenExpiration()
                    : jwtProperties.getAccessTokenExpiration(); // 토큰을 타입으로 구분하여 만료 시간 설정

            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + expiration))
                    .signWith(secretKey)
                    .compact();
        }

        public static Claims getClaims(JwtProperties jwtProperties, String token) {
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
            token = token.replace("Bearer ", "").trim();

            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
    }
  
    public static class random {
        private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        private static final SecureRandom RANDOM = new SecureRandom();

        public static String generateUID(int length) {
            StringBuilder uid = new StringBuilder(length);

            for (int i = 0; i < length; i++) {
                int randomIndex = RANDOM.nextInt(CHARACTERS.length());
                uid.append(CHARACTERS.charAt(randomIndex));
            }

            return uid.toString();

        }
    }

    public static class list {
        public static boolean hasValue(List<?> list) {
            return list != null && !list.isEmpty();
        }
    }
}

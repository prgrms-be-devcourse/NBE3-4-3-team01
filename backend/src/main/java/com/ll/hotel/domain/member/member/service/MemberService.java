package com.ll.hotel.domain.member.member.service;


import com.ll.hotel.domain.member.member.dto.FavoriteDto;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.member.member.dto.JoinRequest;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import com.ll.hotel.global.jwt.dto.GeneratedToken;
import com.ll.hotel.global.jwt.dto.JwtProperties;
import com.ll.hotel.global.request.Rq;
import com.ll.hotel.global.response.RsData;
import com.ll.hotel.global.security.oauth2.entity.OAuth;
import com.ll.hotel.global.security.oauth2.repository.OAuthRepository;
import com.ll.hotel.standard.util.Ut;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ll.hotel.global.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final OAuthRepository oAuthRepository;
    private final AuthTokenService authTokenService;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    private final HotelRepository hotelRepository;
    private final Rq rq;

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private static final String LOGOUT_PREFIX = "LOGOUT:";

    @Transactional
    public Member join(@Valid JoinRequest joinRequest) {
        log.debug("Join service - OAuth 정보: provider={}, oauthId={}", 
                 joinRequest.provider(), joinRequest.oauthId());
                 
        Optional<Member> existingMember = memberRepository.findByMemberEmail(joinRequest.email());
        if (existingMember.isPresent()) {
            Member member = existingMember.get();
            
            // OAuth 정보가 있는 경우에만 저장
            if (joinRequest.provider() != null && joinRequest.oauthId() != null) {
                OAuth oauth = OAuth.builder()
                        .member(member)
                        .provider(joinRequest.provider())
                        .oauthId(joinRequest.oauthId())
                        .build();
                oAuthRepository.save(oauth);
                member.getOauths().add(oauth);
                return member;
            }
            
            EMAIL_ALREADY_EXISTS.throwServiceException();
        }

        Member newMember = Member.builder()
                .memberEmail(joinRequest.email())
                .memberName(joinRequest.name())
                .memberPhoneNumber(joinRequest.phoneNumber())
                .role(joinRequest.role())
                .memberStatus(MemberStatus.ACTIVE)
                .birthDate(joinRequest.birthDate())
                .build();
        
        Member savedMember = memberRepository.save(newMember);
        
        OAuth oauth = OAuth.builder()
                .member(savedMember)
                .provider(joinRequest.provider())
                .oauthId(joinRequest.oauthId())
                .build();
        oAuthRepository.save(oauth);
        
        return savedMember;
    }

    public Optional<Member> findByMemberEmail(String email) {
        return memberRepository.findByMemberEmail(email);
    }

    public boolean existsByMemberEmail(String email) {
        log.debug("Checking if member exists with email: {}", email);
        boolean exists = memberRepository.existsByMemberEmail(email);
        log.debug("Member exists with email {}: {}", email, exists);
        return exists;
    }

    public boolean verifyToken(String accessToken) {
        return authTokenService.verifyToken(accessToken);
    }

    public RsData<String> refreshAccessToken(String refreshToken) {
        return refreshTokenService.refreshAccessToken(refreshToken);
    }

    public String generateRefreshToken(String email) {
        return refreshTokenService.generateRefreshToken(email);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        String refreshToken = null;
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // 액세스 토큰이 만료되었다면 리프레시 토큰으로 처리
        if (accessToken == null && refreshToken != null) {
            RsData<String> refreshResult = refreshAccessToken(refreshToken);
            if (refreshResult.isSuccess()) {
                accessToken = refreshResult.getData();
            }
        }

        if (accessToken == null) {
            UNAUTHORIZED.throwServiceException();
        }

        String email = authTokenService.getEmail(accessToken);
        
        // Redis에서 토큰 무효화
        redisTemplate.opsForValue().set(
            LOGOUT_PREFIX + accessToken,
            email,
            jwtProperties.getAccessTokenExpiration(),
            TimeUnit.MILLISECONDS
        );

        // Refresh 토큰 삭제
        refreshTokenService.removeRefreshToken(email);

        deleteCookie(response);
    }

    private static void deleteCookie(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");

        Cookie roleCookie = new Cookie("role", null);
        roleCookie.setMaxAge(0);
        roleCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");

        Cookie oauth2AuthRequestCookie = new Cookie("oauth2_auth_request", null);
        oauth2AuthRequestCookie.setMaxAge(0);
        oauth2AuthRequestCookie.setPath("/");

        response.addCookie(accessTokenCookie);
        response.addCookie(roleCookie);
        response.addCookie(refreshTokenCookie);
        response.addCookie(oauth2AuthRequestCookie);
    }

    public boolean isLoggedOut(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOGOUT_PREFIX + token));
    }

    public String getEmailFromToken(String token) {
        return authTokenService.getEmail(token);
    }

    public String extractEmailIfValid(String token) {
        if (isLoggedOut(token)) {
            TOKEN_LOGGED_OUT.throwServiceException();
        }
        if (!verifyToken(token)) {
            TOKEN_INVALID.throwServiceException();
        }
        return getEmailFromToken(token);
    }

    @Transactional
    public void addFavorite(Long hotelId) {
        Member actor = rq.getActor();
        if (actor == null) {
            UNAUTHORIZED.throwServiceException();
        }

        Hotel hotel = hotelRepository.findById(hotelId)
            .orElseThrow(HOTEL_NOT_FOUND::throwServiceException);

        if (actor.getFavoriteHotels().contains(hotel)) {
            FAVORITE_ALREADY_EXISTS.throwServiceException();
        }

        actor.getFavoriteHotels().add(hotel);
        hotel.getFavorites().add(actor);
        
        memberRepository.save(actor);
        hotelRepository.save(hotel);
    }

    @Transactional
    public void removeFavorite(Long hotelId) {
        Member actor = rq.getActor();
        if (actor == null) {
            UNAUTHORIZED.throwServiceException();
        }

        Hotel hotel = hotelRepository.findById(hotelId)
            .orElseThrow(HOTEL_NOT_FOUND::throwServiceException);

        if (!actor.getFavoriteHotels().contains(hotel)) {
            FAVORITE_NOT_FOUND.throwServiceException();
        }

        actor.getFavoriteHotels().remove(hotel);
        hotel.getFavorites().remove(actor);
        
        memberRepository.save(actor);
        hotelRepository.save(hotel);
    }

    public List<FavoriteDto> getFavoriteHotels() {
        Member actor = rq.getActor();
        log.debug("getFavoriteHotels - actor: {}", actor);
        
        if (actor == null) {
            UNAUTHORIZED.throwServiceException();
        }

        Set<Hotel> favorites = actor.getFavoriteHotels();
        log.debug("getFavoriteHotels - favorites size: {}", favorites.size());

        return favorites.stream()
            .map(hotel -> {
                log.debug("getFavoriteHotels - hotel: {}", hotel.getHotelName());
                return FavoriteDto.from(hotel);
            })
            .collect(Collectors.toList());
    }
  
    public boolean isFavoriteHotel(long hotelId) {
        Member actor = rq.getActor();

        if (actor == null) {
            UNAUTHORIZED.throwServiceException();
        }

        Set<Hotel> favorites = actor.getFavoriteHotels();

        return favorites.stream()
                .anyMatch(hotel -> hotel.getId() == hotelId);
    }

    @Transactional(readOnly = true)
    public Member findByProviderAndOauthId(String provider, String oauthId) {
        return oAuthRepository.findByProviderAndOauthIdWithMember(provider, oauthId)
            .orElseThrow(OAUTH_NOT_FOUND::throwServiceException)
            .getMember();
    }

    @Transactional
    public void oAuth2Login(Member member, HttpServletResponse response) {
        GeneratedToken tokens = authTokenService.generateToken(
            member.getMemberEmail(), 
            member.getUserRole()
        );
        
        addAuthCookies(response, tokens, member);
    }

    private void addAuthCookies(HttpServletResponse response, GeneratedToken tokens, Member member) {
        // Access Token 쿠키
        Cookie accessTokenCookie = new Cookie("access_token", tokens.accessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키
        Cookie refreshTokenCookie = new Cookie("refresh_token", tokens.refreshToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);

        // Role 쿠키
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("role", member.getUserRole());
        roleData.put("hasHotel", member.getBusiness() != null && member.getBusiness().getHotel() != null);
        roleData.put("hotelId", member.getBusiness() != null && member.getBusiness().getHotel() != null ? 
            member.getBusiness().getHotel().getId() : -1);
        
        Cookie roleCookie = new Cookie("role", URLEncoder.encode(Ut.json.toString(roleData), StandardCharsets.UTF_8));
        roleCookie.setPath("/");
        response.addCookie(roleCookie);
    }
}
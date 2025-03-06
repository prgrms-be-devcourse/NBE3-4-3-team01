package com.ll.hotel.domain.member.member.controller;

import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.request.Rq;
import com.ll.hotel.global.response.RsData;
import com.ll.hotel.global.security.oauth2.entity.OAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class OAuth2Controller {
    private final Rq rq;

    @GetMapping("/oauth2/callback") // 사용하지 않는 컨트롤러 (임시 테스트를 위해 일단 보류)
    public RsData<OAuth2Response> callback(
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String refreshToken,
            @RequestParam String status
    ) {
        if (!"SUCCESS".equals(status)) {
            return RsData.success(HttpStatus.BAD_REQUEST, new OAuth2Response(
                null, null, status, null, null, null,
                false, false, false, null, null
            ));
        }

        Member actor = rq.getActor();
        if (actor == null) {
            return RsData.success(HttpStatus.UNAUTHORIZED, new OAuth2Response(
                null, null, status, null, null, null,
                false, false, false, null, null
            ));
        }

        OAuth oAuth = actor.getFirstOAuth();
        if (oAuth == null) {
            return RsData.success(HttpStatus.BAD_REQUEST, new OAuth2Response(
                actor.getMemberEmail(), actor.getMemberName(), status,
                actor.getUserRole(), null, null,
                actor.isUser(), actor.isAdmin(), actor.isBusiness(),
                accessToken, refreshToken
            ));
        }

        OAuth2Response response = new OAuth2Response(
            actor.getMemberEmail(),
            actor.getMemberName(),
            status,
            actor.getUserRole(),
            oAuth.getProvider(),
            oAuth.getOauthId(),
            actor.isUser(),
            actor.isAdmin(),
            actor.isBusiness(),
            accessToken,
            refreshToken
        );

        return RsData.success(HttpStatus.OK, response);
    }
}

record OAuth2Response(
    String email,
    String name,
    String status,
    String role,
    String provider,
    String oauthId,
    boolean isUser,
    boolean isAdmin,
    boolean isBusiness,
    String accessToken,
    String refreshToken
) {} 
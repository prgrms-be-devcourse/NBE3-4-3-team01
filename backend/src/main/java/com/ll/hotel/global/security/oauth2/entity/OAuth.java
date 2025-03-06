package com.ll.hotel.global.security.oauth2.entity;

import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.jpa.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "oauth", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "oauth_id"})
})
public class OAuth extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String provider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    public static OAuth create(Member member, String provider, String oauthId) {
        return OAuth.builder()
                .member(member)
                .provider(provider)
                .oauthId(oauthId)
                .build();
    }
}

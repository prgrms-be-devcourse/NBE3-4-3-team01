package com.ll.hotel.domain.member.member.entity;

import static com.ll.hotel.global.exceptions.ErrorCode.BUSINESS_ACCESS_FORBIDDEN;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import com.ll.hotel.global.jpa.entity.BaseTime;
import com.ll.hotel.global.security.oauth2.entity.OAuth;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "member",
    indexes = {
        @Index(name = "idx_member_email", columnList = "memberEmail")
    }
)
public class Member extends BaseTime {

    @Column(unique = true, nullable = false)
    private String memberEmail;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String memberPhoneNumber;

    @Column(nullable = true)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OAuth> oauths = new ArrayList<>();

    @OneToOne(mappedBy="member", fetch = FetchType.LAZY)
    private Business business;

    @ManyToMany
    @JoinTable(
        name = "favorite",
        joinColumns = @JoinColumn(name = "member_id"),
        inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    @Builder.Default
    private Set<Hotel> favoriteHotels = new HashSet<>();

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isBusiness() {
        return this.role == Role.BUSINESS;
    }

    public boolean isUser() {
        return this.role == Role.USER;
    }

    public String getUserRole() {
        return this.role.name();
    }

    public OAuth getFirstOAuth() {
        return this.oauths.isEmpty() ? null : this.oauths.get(0);
    }

    public void checkBusiness() {
        if (!this.isBusiness()) {
            BUSINESS_ACCESS_FORBIDDEN.throwServiceException();
        }
    }
}

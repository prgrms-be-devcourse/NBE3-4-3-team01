package com.ll.hotel.domain.hotel.hotel.entity;

import com.ll.hotel.domain.hotel.hotel.dto.PostHotelRequest;
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hotel extends BaseTime {
    @Column
    private String hotelName;

    @Column(unique = true)
    private String hotelEmail;

    @Column
    private String hotelPhoneNumber;

    @Column
    private String streetAddress;

    @Column
    private Integer zipCode;

    @Column
    private Integer hotelGrade;

    @Column
    private LocalTime checkInTime;

    @Column
    private LocalTime checkOutTime;

    @Column(columnDefinition = "TEXT")
    private String hotelExplainContent;

    @Column
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private HotelStatus hotelStatus = HotelStatus.PENDING;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    private Business business;

    @ManyToMany
    Set<HotelOption> hotelOptions;

    @ManyToMany
    Set<Member> favorites;

    @Column(nullable = false)
    private Double averageRating;

    @Column(nullable = false)
    private Long totalReviewRatingSum;

    @Column(nullable = false)
    private Long totalReviewCount;

    // 평균 레이팅 업데이트
    public void updateAverageRating(int countOffset, int ratingOffset) {
        this.totalReviewCount += countOffset;
        this.totalReviewRatingSum += ratingOffset;
        this.averageRating = Math.round(((double) totalReviewRatingSum / totalReviewCount) * 10.0) / 10.0;
    }

    public boolean isOwnedBy(Member member) {
        return this.business != null && this.business.getMember().equals(member);
    }

    /**
     * 불필요 시 삭제
     */
    @PreRemove
    private void preRemove() {
        if (this.business != null) {
            this.business.setHotel(null);
        }
    }

    @PrePersist
    public void prePersist() {
        if (averageRating == null) {
            averageRating = 0.0;
        }
        if (totalReviewRatingSum == null) {
            totalReviewRatingSum = 0L;
        }
        if (totalReviewCount == null) {
            totalReviewCount = 0L;
        }
    }

    public static Hotel hotelBuild(PostHotelRequest request, Business business, Set<HotelOption> hotelOptions) {
        return Hotel.builder()
                .hotelName(request.hotelName())
                .hotelEmail(request.hotelEmail())
                .hotelPhoneNumber(request.hotelPhoneNumber())
                .streetAddress(request.streetAddress())
                .zipCode(request.zipCode())
                .hotelGrade(request.hotelGrade())
                .checkInTime(request.checkInTime())
                .checkOutTime(request.checkOutTime())
                .hotelExplainContent(request.hotelExplainContent())
                .business(business)
                .hotelOptions(hotelOptions)
                .build();
    }
}

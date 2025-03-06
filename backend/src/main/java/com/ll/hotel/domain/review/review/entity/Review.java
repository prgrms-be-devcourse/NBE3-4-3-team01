package com.ll.hotel.domain.review.review.entity;

import com.ll.hotel.domain.booking.booking.entity.Booking;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.review.comment.entity.ReviewComment;
import com.ll.hotel.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@NoArgsConstructor
@Getter
@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
public class Review extends BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "호텔 정보는 필수입니다.")
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "객실 정보는 필수입니다.")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "예약 정보는 필수입니다.")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "작성자 정보는 필수입니다.")
    private Member member;

    @Setter
    @OneToOne(mappedBy = "review", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private ReviewComment reviewComment;

    @Column(length = 300, nullable = false)
    @Setter
    @NotNull(message = "리뷰 내용은 필수입니다.")
    @Size(min = 5, max = 300, message = "리뷰 내용은 5자 이상, 300자 이하여야 합니다.")
    private String content;

    @Column(nullable = false)
    @Setter
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점이어야 합니다.")
    private Integer rating;

    @Builder
    public Review(Hotel hotel, Room room, Booking booking, Member member, String content, Integer rating) {
        this.hotel = hotel;
        this.room = room;
        this.booking = booking;
        this.member = member;
        this.content = content;
        this.rating = rating;
    }

    public boolean isWrittenBy(Member member) {
        return this.member.getId().equals(member.getId());
    }
}
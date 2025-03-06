package com.ll.hotel.domain.booking.payment.entity;

import com.ll.hotel.domain.booking.payment.type.PaymentStatus;
import com.ll.hotel.global.jpa.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseTime {
    /*
     * 결제 요청/취소 시 merchant_uid가 필요
     * merchant_uid는 상점(호텔) 측에서 생성하는 주문번호
     * api key는 application.yml에 작성
     */
    @NotNull
    @Column(unique = true)
    private String merchantUid;

    @NotNull
    @Column
    private int amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PAID;

    @NotNull
    @Column
    private LocalDateTime paidAt;
}

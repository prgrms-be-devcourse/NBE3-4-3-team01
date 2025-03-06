package com.ll.hotel.domain.member.member.entity;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;
import com.ll.hotel.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business extends BaseEntity {

    @NotBlank(message = "사업자 등록 번호는 필수 항목입니다.")
    @Pattern(regexp = "^[0-9]{10}$", message = "사업자 등록 번호는 10자리 숫자여야 합니다.")
    @Column(name = "business_registration_number", nullable = false, unique = true)
    private String businessRegistrationNumber;

    @NotNull(message = "개업 일자는 필수 항목입니다.")
    @PastOrPresent(message = "개업 일자는 현재 날짜 또는 과거여야 합니다.")
    @Column(name = "business_start_date", nullable = false)
    private LocalDate startDate;

    @NotBlank(message = "대표자명은 필수 항목입니다.")
    @Size(max = 30, message = "대표자명은 최대 30자까지 가능합니다.")
    @Column(name = "business_owner_name", nullable = false, length = 30)
    private String ownerName;

    @NotNull(message = "사업자 승인 상태는 필수 항목입니다.")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "business_approval_status", nullable = false)
    private BusinessApprovalStatus approvalStatus = BusinessApprovalStatus.PENDING;

    @NotNull(message = "회원 정보는 필수 항목입니다.")
    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(mappedBy = "business", fetch = FetchType.LAZY)
    private Hotel hotel;
}

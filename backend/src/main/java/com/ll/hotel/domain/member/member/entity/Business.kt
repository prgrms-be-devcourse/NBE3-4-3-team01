package com.ll.hotel.domain.member.member.entity

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus
import com.ll.hotel.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDate

@Entity
class Business (
    @field:NotBlank
    @field:Pattern(regexp = "^[0-9]{10}$")
    @Column(name = "business_registration_number", nullable = false, unique = true)
    var businessRegistrationNumber: String,

    @field:NotNull
    @field:PastOrPresent
    @Column(name = "business_start_date", nullable = false)
    var startDate: LocalDate,

    @field:NotBlank
    @field:Size(max = 30)
    @Column(name = "business_owner_name", nullable = false, length = 30)
    var ownerName: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    @Column(name = "business_approval_status", nullable = false)
    var approvalStatus: BusinessApprovalStatus = BusinessApprovalStatus.PENDING,

    @field:NotNull
    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @OneToOne(mappedBy = "business", fetch = FetchType.LAZY)
    var hotel: Hotel? = null
): BaseEntity()
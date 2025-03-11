package com.ll.hotel.domain.member.member.entity

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ErrorCode.BUSINESS_ACCESS_FORBIDDEN
import com.ll.hotel.global.jpa.entity.BaseTime
import com.ll.hotel.global.security.oauth2.entity.OAuth
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "member",
    indexes = [
        Index(name = "idx_member_email", columnList = "memberEmail")
    ]
)
class Member(
    @Column(unique = true, nullable = false)
    var memberEmail: String = "",

    @Column(nullable = false)
    var memberName: String = "",

    @Column(nullable = false)
    var memberPhoneNumber: String = "",

    @Column(nullable = true)
    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var memberStatus: MemberStatus = MemberStatus.ACTIVE,

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL])
    var oauths: MutableList<OAuth> = ArrayList(),

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    var business: Business? = null,

    @ManyToMany
    @JoinTable(
        name = "favorite",
        joinColumns = [JoinColumn(name = "member_id")],
        inverseJoinColumns = [JoinColumn(name = "hotel_id")]
    )
    var favoriteHotels: MutableSet<Hotel> = HashSet()
) : BaseTime() {

    val isAdmin: Boolean
        get() = this.role == Role.ADMIN

    val isBusiness: Boolean
        get() = this.role == Role.BUSINESS

    val isUser: Boolean
        get() = this.role == Role.USER

    fun getUserRole(): String {
        return this.role.name
    }

    fun getFirstOAuth(): OAuth? {
        return if (this.oauths.isEmpty()) null else this.oauths[0]
    }

    fun checkBusiness() {
        if (!this.isBusiness) {
            BUSINESS_ACCESS_FORBIDDEN.throwServiceException()
        }
    }
} 
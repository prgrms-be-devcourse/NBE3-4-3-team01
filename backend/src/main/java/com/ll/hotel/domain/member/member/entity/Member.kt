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
class Member : BaseTime() {
    @Column(unique = true, nullable = false)
    var memberEmail: String = ""

    @Column(nullable = false)
    var memberName: String = ""

    @Column(nullable = false)
    var memberPhoneNumber: String = ""

    @Column(nullable = true)
    var birthDate: LocalDate? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var memberStatus: MemberStatus = MemberStatus.ACTIVE

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL])
    var oauths: MutableList<OAuth> = ArrayList()

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    var business: Business? = null

    @ManyToMany
    @JoinTable(
        name = "favorite",
        joinColumns = [JoinColumn(name = "member_id")],
        inverseJoinColumns = [JoinColumn(name = "hotel_id")]
    )
    var favoriteHotels: MutableSet<Hotel> = HashSet()

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

    companion object {
        @JvmStatic
        fun builder(): MemberBuilder {
            return MemberBuilder()
        }
    }

    class MemberBuilder {
        private var memberEmail: String = ""
        private var memberName: String = ""
        private var memberPhoneNumber: String = ""
        private var birthDate: LocalDate? = null
        private var role: Role = Role.USER
        private var memberStatus: MemberStatus = MemberStatus.ACTIVE
        private var oauths: MutableList<OAuth> = ArrayList()
        private var business: Business? = null
        private var favoriteHotels: MutableSet<Hotel> = HashSet()

        fun memberEmail(memberEmail: String): MemberBuilder {
            this.memberEmail = memberEmail
            return this
        }

        fun memberName(memberName: String): MemberBuilder {
            this.memberName = memberName
            return this
        }

        fun memberPhoneNumber(memberPhoneNumber: String): MemberBuilder {
            this.memberPhoneNumber = memberPhoneNumber
            return this
        }

        fun birthDate(birthDate: LocalDate?): MemberBuilder {
            this.birthDate = birthDate
            return this
        }

        fun role(role: Role): MemberBuilder {
            this.role = role
            return this
        }

        fun memberStatus(memberStatus: MemberStatus): MemberBuilder {
            this.memberStatus = memberStatus
            return this
        }

        fun oauths(oauths: MutableList<OAuth>): MemberBuilder {
            this.oauths = oauths
            return this
        }

        fun business(business: Business?): MemberBuilder {
            this.business = business
            return this
        }

        fun favoriteHotels(favoriteHotels: MutableSet<Hotel>): MemberBuilder {
            this.favoriteHotels = favoriteHotels
            return this
        }

        fun build(): Member {
            val member = Member()
            member.memberEmail = this.memberEmail
            member.memberName = this.memberName
            member.memberPhoneNumber = this.memberPhoneNumber
            member.birthDate = this.birthDate
            member.role = this.role
            member.memberStatus = this.memberStatus
            member.oauths = this.oauths
            member.business = this.business
            member.favoriteHotels = this.favoriteHotels
            return member
        }
    }
} 
package com.ll.hotel.global.security.oauth2.entity

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.global.jpa.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "oauth", 
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "oauth_id"])
    ]
)
class OAuth : BaseEntity() {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null
    
    @Column(nullable = false)
    var provider: String = ""
    
    @Column(name = "oauth_id", nullable = false)
    var oauthId: String = ""
    
    companion object {
        @JvmStatic
        fun create(member: Member, provider: String, oauthId: String): OAuth {
            val oauth = OAuth()
            oauth.member = member
            oauth.provider = provider
            oauth.oauthId = oauthId
            return oauth
        }
        
        @JvmStatic
        fun builder(): OAuthBuilder {
            return OAuthBuilder()
        }
    }
    
    class OAuthBuilder {
        private var member: Member? = null
        private var provider: String = ""
        private var oauthId: String = ""
        
        fun member(member: Member): OAuthBuilder {
            this.member = member
            return this
        }
        
        fun provider(provider: String): OAuthBuilder {
            this.provider = provider
            return this
        }
        
        fun oauthId(oauthId: String): OAuthBuilder {
            this.oauthId = oauthId
            return this
        }
        
        fun build(): OAuth {
            val oauth = OAuth()
            oauth.member = this.member
            oauth.provider = this.provider
            oauth.oauthId = this.oauthId
            return oauth
        }
    }
} 
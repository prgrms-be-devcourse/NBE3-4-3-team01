package com.ll.hotel.domain.member.member.type

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "business-api")
class BusinessApiProperties {
    lateinit var serviceKey: String
    lateinit var validationUrl: String
    lateinit var statusUrl: String
}
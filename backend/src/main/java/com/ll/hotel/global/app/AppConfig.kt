package com.ll.hotel.global.app

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@ConfigurationProperties(prefix = "app")
class AppConfig {

    var mode: String = ""

    companion object {
        @JvmStatic
        lateinit var objectMapper: ObjectMapper
            private set
    }

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        AppConfig.objectMapper = objectMapper
    }

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
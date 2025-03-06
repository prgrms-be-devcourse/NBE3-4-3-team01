package com.ll.hotel.domain.member.member.type;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "business-api")
public class BusinessApiProperties {
    private String serviceKey;
    private String validationUrl;
    private String statusUrl;
}

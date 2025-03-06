package com.ll.hotel.global.security.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "custom.redis")
public class RedisProperties {

    private String host;
    private int port;
    private String password;
}
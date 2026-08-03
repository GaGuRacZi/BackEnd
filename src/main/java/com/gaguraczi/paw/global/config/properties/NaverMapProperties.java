package com.gaguraczi.paw.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.maps")
public record NaverMapProperties(
        String clientId,
        String clientSecret
) {
}

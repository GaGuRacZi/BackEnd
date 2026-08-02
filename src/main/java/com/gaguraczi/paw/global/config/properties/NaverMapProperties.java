package com.gaguraczi.paw.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "naver.maps")
public class NaverMapProperties {

    private String clientId;
    private String clientSecret;
}

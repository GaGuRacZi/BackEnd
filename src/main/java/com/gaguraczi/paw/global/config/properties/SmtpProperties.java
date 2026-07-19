package com.gaguraczi.paw.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "smtp")
public class SmtpProperties {

    private String host = "smtp.gmail.com";
    private int port = 587;
    private boolean useTls = true;
    private boolean useSsl = false;
    private boolean skipCertVerify = false;
    private int timeoutSeconds = 20;
    private String username = "";
    private String password = "";
    private String fromEmail = "";
}

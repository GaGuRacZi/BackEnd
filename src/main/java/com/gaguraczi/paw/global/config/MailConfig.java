package com.gaguraczi.paw.global.config;

import com.gaguraczi.paw.global.config.properties.SmtpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(SmtpProperties smtp) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtp.getHost());
        mailSender.setPort(smtp.getPort());
        mailSender.setUsername(smtp.getUsername());
        mailSender.setPassword(smtp.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", String.valueOf(smtp.getTimeoutSeconds() * 1000));
        props.put("mail.smtp.timeout", String.valueOf(smtp.getTimeoutSeconds() * 1000));
        props.put("mail.smtp.writetimeout", String.valueOf(smtp.getTimeoutSeconds() * 1000));

        if (smtp.isUseTls()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        if (smtp.isUseSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        if (smtp.isSkipCertVerify()) {
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.ssl.checkserveridentity", "false");
        }

        return mailSender;
    }
}

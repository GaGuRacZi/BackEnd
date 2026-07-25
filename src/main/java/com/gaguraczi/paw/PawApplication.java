package com.gaguraczi.paw;

import com.gaguraczi.paw.global.config.properties.JwtProperties;
import com.gaguraczi.paw.global.config.properties.KakaoProperties;
import com.gaguraczi.paw.global.config.properties.SmtpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, KakaoProperties.class, SmtpProperties.class})
public class PawApplication {

	/**
	 * Starts the Paw Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(PawApplication.class, args);
	}

}

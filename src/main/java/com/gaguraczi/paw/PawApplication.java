package com.gaguraczi.paw;

import com.gaguraczi.paw.global.config.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class})
public class PawApplication {

	public static void main(String[] args) {
		SpringApplication.run(PawApplication.class, args);
	}

}

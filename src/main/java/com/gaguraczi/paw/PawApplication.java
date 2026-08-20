package com.gaguraczi.paw;

import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.medication.config.MedicationProperties;
import com.gaguraczi.paw.domain.rag.config.RagProperties;
import com.gaguraczi.paw.global.config.properties.JwtProperties;
import com.gaguraczi.paw.global.config.properties.KakaoProperties;
import com.gaguraczi.paw.global.config.properties.NaverMapProperties;
import com.gaguraczi.paw.global.config.properties.SmtpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		JwtProperties.class,
		KakaoProperties.class,
		SmtpProperties.class,
		NaverMapProperties.class,
		RagProperties.class,
		MedicationProperties.class,
		VisitProperties.class
})
public class PawApplication {

	/**
	 * Starts the Paw Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(PawApplication.class, args);
	}

}

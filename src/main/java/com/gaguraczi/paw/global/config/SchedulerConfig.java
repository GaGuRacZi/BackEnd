package com.gaguraczi.paw.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.gaguraczi.paw.global.time.AppTime;

import java.time.Clock;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public Clock clock() {
        return Clock.system(AppTime.KST);
    }
}

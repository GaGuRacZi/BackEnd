package com.gaguraczi.paw.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String ALARM_TASK_EXECUTOR = "alarmTaskExecutor";
    public static final String VISIT_TASK_EXECUTOR = "visitTaskExecutor";

    @Bean(name = ALARM_TASK_EXECUTOR)
    public Executor alarmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("alarm-fcm-");
        executor.initialize();
        return executor;
    }

    @Bean(name = VISIT_TASK_EXECUTOR)
    public Executor visitTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("visit-stt-");
        executor.initialize();
        return executor;
    }
}

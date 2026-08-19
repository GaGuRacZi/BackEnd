package com.gaguraczi.paw.global.config;

import com.openai.core.Timeout;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAiHttpClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    @Bean
    public OpenAiHttpClientBuilderCustomizer openAiChatHttpTimeoutCustomizer() {
        return builder -> builder.timeout(Timeout.builder()
                .connect(CONNECT_TIMEOUT)
                .read(READ_TIMEOUT)
                .request(READ_TIMEOUT)
                .build());
    }
}

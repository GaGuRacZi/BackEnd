package com.gaguraczi.paw.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Jackson 2 mapper for remaining Jackson-2 callers (cookies, login-link, JPA converters).
     * HTTP JSON uses Boot 4's Jackson 3 {@code JsonMapper}; this bean must still handle Java Time
     * so LocalDateTime is not rejected with a misleading domain error.
     *
     * @return a Jackson 2 object mapper with Java Time support
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}

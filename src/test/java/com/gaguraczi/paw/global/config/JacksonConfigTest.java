package com.gaguraczi.paw.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    @Test
    void jackson2ObjectMapper는_LocalDateTime을_ISO문자열로_쓴다() throws Exception {
        ObjectMapper mapper = new JacksonConfig().objectMapper();

        String json = mapper.writeValueAsString(LocalDateTime.of(2026, 8, 21, 6, 38));

        assertThat(json).contains("2026-08-21T06:38:00");
        assertThat(mapper.readValue("\"2026-08-21\"", LocalDate.class))
                .isEqualTo(LocalDate.of(2026, 8, 21));
    }
}

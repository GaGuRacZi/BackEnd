package com.gaguraczi.paw.domain.todo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(70)
@RequiredArgsConstructor
public class TodoDateSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        try {
            String sql = StreamUtils.copyToString(
                    new ClassPathResource("db/todo-date-due-index.sql").getInputStream(),
                    StandardCharsets.UTF_8);
            for (String statement : sql.split(";;")) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                jdbcTemplate.execute(trimmed);
            }
            log.info("Applied todo_date remind_at due index");
        } catch (Exception e) {
            log.error("Failed to apply todo_date due index (todo_date may not exist yet)", e);
        }
    }
}

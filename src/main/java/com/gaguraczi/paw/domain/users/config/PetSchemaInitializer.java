package com.gaguraczi.paw.domain.users.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures a partial unique index so each user has at most one main pet.
 * Runs after Hibernate DDL (ddl-auto=update) because pet may not exist during schema-locations init.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PetSchemaInitializer implements ApplicationRunner {

    private static final String SQL = """
            CREATE UNIQUE INDEX IF NOT EXISTS uk_pet_one_main_per_user
            ON pet (uid)
            WHERE is_main = true
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(SQL);
            log.info("Ensured partial unique index uk_pet_one_main_per_user");
        } catch (Exception e) {
            log.warn("Failed to create uk_pet_one_main_per_user (table may not exist yet): {}", e.getMessage());
        }
    }
}

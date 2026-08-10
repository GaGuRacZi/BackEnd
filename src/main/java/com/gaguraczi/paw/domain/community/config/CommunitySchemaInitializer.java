package com.gaguraczi.paw.domain.community.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Statement;

/**
 * Seeds Figma community tags and installs comment schema guards after Hibernate DDL.
 * Tag seeding runs in its own transaction; JDBC DDL runs outside that boundary
 * (required for CREATE/DROP INDEX CONCURRENTLY).
 */
@Slf4j
@Component
@Order(60)
@RequiredArgsConstructor
public class CommunitySchemaInitializer implements ApplicationRunner {

    private final CommunityTagSeeder communityTagSeeder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        communityTagSeeder.seedTags();
        applyCycleGuard();
        applyCommentPostCreatedIndex();
    }

    private void applyCycleGuard() {
        try {
            executeClasspathSql("db/comment-cycle-guard.sql", false);
            log.info("Applied comment cycle-guard trigger");
        } catch (Exception e) {
            log.error("Failed to apply comment cycle-guard (comment table may not exist yet)", e);
        }
    }

    private void applyCommentPostCreatedIndex() {
        try {
            executeClasspathSql("db/comment-post-created-index.sql", true);
            log.info("Applied comment post/created index");
        } catch (Exception e) {
            log.error("Failed to apply comment post/created index (comment table may not exist yet)", e);
        }
    }

    private void executeClasspathSql(String classpathLocation, boolean forceAutocommit) throws Exception {
        String sql = StreamUtils.copyToString(
                new ClassPathResource(classpathLocation).getInputStream(),
                StandardCharsets.UTF_8);
        for (String statement : sql.split(";;")) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (forceAutocommit) {
                executeOutsideTransaction(trimmed);
            } else {
                jdbcTemplate.execute(trimmed);
            }
        }
    }

    private void executeOutsideTransaction(String sql) {
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            boolean previous = connection.getAutoCommit();
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            } finally {
                connection.setAutoCommit(previous);
            }
            return null;
        });
    }
}

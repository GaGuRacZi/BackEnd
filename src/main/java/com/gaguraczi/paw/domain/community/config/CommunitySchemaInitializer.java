package com.gaguraczi.paw.domain.community.config;

import com.gaguraczi.paw.domain.community.entity.CommunityTag;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.repository.CommunityTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

/**
 * Seeds Figma community tags and installs comment cycle-guard after Hibernate DDL.
 */
@Slf4j
@Component
@Order(60)
@RequiredArgsConstructor
public class CommunitySchemaInitializer implements ApplicationRunner {

    private final CommunityTagRepository communityTagRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        seedTags();
        applyCycleGuard();
    }

    private void seedTags() {
        int inserted = 0;
        for (CommunityTagCode code : CommunityTagCode.values()) {
            if (communityTagRepository.existsByPostTypeAndTagCode(code.getPostType(), code.name())) {
                continue;
            }
            communityTagRepository.save(CommunityTag.builder()
                    .postType(code.getPostType())
                    .tagName(code.getTagName())
                    .tagCode(code.name())
                    .sortOrder(code.getSortOrder())
                    .isActive(true)
                    .build());
            inserted++;
        }
        log.info("Community tags seeded (inserted={})", inserted);
    }

    private void applyCycleGuard() {
        try {
            String sql = StreamUtils.copyToString(
                    new ClassPathResource("db/comment-cycle-guard.sql").getInputStream(),
                    StandardCharsets.UTF_8);
            for (String statement : sql.split(";;")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    jdbcTemplate.execute(trimmed);
                }
            }
            log.info("Applied comment cycle-guard trigger");
        } catch (Exception e) {
            log.error("Failed to apply comment cycle-guard (comment table may not exist yet)", e);
        }
    }
}

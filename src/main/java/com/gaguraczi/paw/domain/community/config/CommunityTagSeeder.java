package com.gaguraczi.paw.domain.community.config;

import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.repository.CommunityTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityTagSeeder {

    private final CommunityTagRepository communityTagRepository;

    @Transactional
    public void seedTags() {
        int inserted = 0;
        for (CommunityTagCode code : CommunityTagCode.values()) {
            int rows = communityTagRepository.insertIgnore(
                    code.getTagName(),
                    code.name(),
                    code.getPostType().name(),
                    code.getSortOrder()
            );
            if (rows > 0) {
                inserted++;
            }
        }
        log.info("Community tags seeded (inserted={})", inserted);
    }
}

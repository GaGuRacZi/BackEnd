package com.gaguraczi.paw.domain.community.config;

import com.gaguraczi.paw.domain.community.entity.CommunityTag;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.repository.CommunityTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityTagSeeder {

    private final CommunityTagRepository communityTagRepository;

    @Transactional
    public void seedTags() {
        List<CommunityTag> existing = communityTagRepository.findAll();
        Set<String> existingKeys = new HashSet<>(existing.size());
        for (CommunityTag tag : existing) {
            existingKeys.add(key(tag.getPostType(), tag.getTagCode()));
        }

        int inserted = 0;
        for (CommunityTagCode code : CommunityTagCode.values()) {
            if (existingKeys.contains(key(code.getPostType(), code.name()))) {
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

    private static String key(PostType postType, String tagCode) {
        return postType.name() + "|" + tagCode;
    }
}

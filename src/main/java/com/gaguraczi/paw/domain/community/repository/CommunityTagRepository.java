package com.gaguraczi.paw.domain.community.repository;

import com.gaguraczi.paw.domain.community.entity.CommunityTag;
import com.gaguraczi.paw.domain.community.enums.PostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityTagRepository extends JpaRepository<CommunityTag, Long> {

    List<CommunityTag> findByPostTypeAndIsActiveTrueOrderBySortOrderAsc(PostType postType);

    Optional<CommunityTag> findByPostTypeAndTagCode(PostType postType, String tagCode);

    boolean existsByPostTypeAndTagCode(PostType postType, String tagCode);
}

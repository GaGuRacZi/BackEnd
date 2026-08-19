package com.gaguraczi.paw.domain.community.repository;

import com.gaguraczi.paw.domain.community.entity.CommunityTag;
import com.gaguraczi.paw.domain.community.enums.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityTagRepository extends JpaRepository<CommunityTag, Long> {

    List<CommunityTag> findByPostTypeAndIsActiveTrueOrderBySortOrderAsc(PostType postType);

    Optional<CommunityTag> findByPostTypeAndTagCode(PostType postType, String tagCode);

    boolean existsByPostTypeAndTagCode(PostType postType, String tagCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO community_tag (tag_name, tag_code, post_type, sort_order, is_active, created_at, updated_at)
            VALUES (:tagName, :tagCode, :postType, :sortOrder, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (post_type, tag_code) DO NOTHING
            """, nativeQuery = true)
    int insertIgnore(
            @Param("tagName") String tagName,
            @Param("tagCode") String tagCode,
            @Param("postType") String postType,
            @Param("sortOrder") int sortOrder
    );
}

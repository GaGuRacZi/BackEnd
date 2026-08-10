package com.gaguraczi.paw.domain.todo.repository;

import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findAllByUser_UidOrderByTagNameAsc(UUID uid);

    Optional<TagEntity> findByTagIdAndUser_Uid(Long tagId, UUID uid);

    boolean existsByUser_UidAndTagName(UUID uid, String tagName);

    boolean existsByUser_UidAndTagNameAndTagIdNot(UUID uid, String tagName, Long tagId);
}
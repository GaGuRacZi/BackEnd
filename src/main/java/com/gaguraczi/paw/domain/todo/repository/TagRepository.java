package com.gaguraczi.paw.domain.todo.repository;

import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findAllByUser_UidOrderByTagNameAsc(String uid);

    Optional<TagEntity> findByTagIdAndUser_Uid(Long tagId, String uid);

    boolean existsByUser_UidAndTagName(String uid, String tagName);

    boolean existsByUser_UidAndTagNameAndTagIdNot(String uid, String tagName, Long tagId);
}
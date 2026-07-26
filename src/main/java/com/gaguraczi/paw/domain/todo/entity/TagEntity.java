package com.gaguraczi.paw.domain.todo.entity;

import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

public class TagEntity {
    @Entity
    @Getter
    @SuperBuilder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Table(name = "tag")
    public static class Tag extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "tag_id")
        private Long tagId;

        @Column(name = "tag_name", length = 36, nullable = false)
        private String tagName;

        @Enumerated(EnumType.STRING)
        @Column(name = "tag_color", nullable = false)
        private TagColorEnum.TagColor tagColor;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "uid", nullable = false)
        private User user;
    }
}

package com.gaguraczi.paw.domain.todo.entity;

import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Table(
            name = "tag",
            uniqueConstraints = @UniqueConstraint(
                    name = "uid_name",
                    columnNames = {"uid", "tag_name"}
            )
            //태그명 중복 피하려고
    )
    public class TagEntity extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "tag_id")
        private Long tagId;

        @Column(name = "tag_name", length = 36, nullable = false)
        private String tagName;

        @Enumerated(EnumType.STRING)
        @Column(name = "tag_color", nullable = false)
        private TagColorEnum tagColorEnum;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "uid", nullable = false)
        private User user;

        public static TagEntity create(User user, String tagName, TagColorEnum tagColorEnum) {
            TagEntity tag = new TagEntity();
            tag.user = user;
            tag.tagName = tagName;
            tag.tagColorEnum = tagColorEnum;
            return tag;
        }

        public void change(String tagName, TagColorEnum tagColorEnum) {
            this.tagName = tagName;
            this.tagColorEnum = tagColorEnum;
    }
        }



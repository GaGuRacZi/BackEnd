package com.gaguraczi.paw.domain.community.entity;

import com.gaguraczi.paw.domain.category.entity.Category;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.code.GeneralErrorCode;
import com.gaguraczi.paw.global.entity.BaseEntity;
import com.gaguraczi.paw.global.exception.GeneralException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community")
public class Community extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @Column(name = "title", nullable = false)
    private String title;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void update(String title, String content, PostType postType) {
        if (title == null || title.isBlank()
                || content == null || content.isBlank()
                || postType == null) {
            throw GeneralException.of(GeneralErrorCode.BAD_REQUEST);
        }
        this.title = title;
        this.content = content;
        this.postType = postType;
    }
}

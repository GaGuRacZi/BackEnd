package com.gaguraczi.paw.domain.community.entity;

import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import com.gaguraczi.paw.global.exception.GeneralException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "comment",
        indexes = {
                @Index(name = "idx_comment_post_id", columnList = "post_id"),
                @Index(name = "idx_comment_parent_id", columnList = "parent_id"),
                @Index(name = "idx_comment_uid_created", columnList = "uid, created_at")
        }
)
public class Comment extends BaseEntity {

    private static final int MAX_ANCESTOR_WALK = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "is_del", nullable = false)
    private Boolean isDel = false;

    /**
     * Attaches this comment under {@code parent}, allowing nested replies.
     * Rejects self-parenting and ancestor cycles (DB trigger is the final safety net).
     */
    public void attachTo(Comment parent) {
        if (parent == null) {
            this.parent = null;
            return;
        }
        if (this == parent || Objects.equals(this.commentId, parent.commentId)) {
            throw GeneralException.of(CommunityErrorCode.COMMENT_CYCLE_400);
        }
        if (this.community == null
                || parent.community == null
                || !Objects.equals(this.community.getPostId(), parent.community.getPostId())) {
            throw GeneralException.of(CommunityErrorCode.COMMENT_POST_MISMATCH_400);
        }

        Comment cursor = parent;
        int depth = 0;
        while (cursor != null) {
            if (this == cursor || Objects.equals(this.commentId, cursor.commentId)) {
                throw GeneralException.of(CommunityErrorCode.COMMENT_CYCLE_400);
            }
            depth++;
            if (depth > MAX_ANCESTOR_WALK) {
                throw GeneralException.of(CommunityErrorCode.COMMENT_CYCLE_400);
            }
            cursor = cursor.parent;
        }
        this.parent = parent;
    }

    public void softDelete() {
        this.isDel = true;
    }

    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw GeneralException.of(CommunityErrorCode.COMMENT_CONTENT_400);
        }
        this.content = content;
    }
}

package com.gaguraczi.paw.domain.community.entity;

import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "community_tag",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_community_tag_post_type_code", columnNames = {"post_type", "tag_code"})
        },
        indexes = {
                @Index(name = "idx_community_tag_post_type_active_sort", columnList = "post_type, is_active, sort_order")
        }
)
public class CommunityTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    @Column(name = "tag_code", nullable = false, length = 50)
    private String tagCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private PostType postType;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @OneToMany(mappedBy = "communityTag")
    private List<Community> communities = new ArrayList<>();
}

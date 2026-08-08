package com.gaguraczi.paw.domain.community.entity;

import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeMethod;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.domain.like.entity.CommunityLike;
import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.converter.StringListConverter;
import com.gaguraczi.paw.global.entity.BaseEntity;
import com.gaguraczi.paw.global.exception.GeneralException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "community",
        indexes = {
                @Index(name = "idx_community_post_type_created", columnList = "post_type, created_at"),
                @Index(name = "idx_community_tag_created", columnList = "tag_id, created_at"),
                @Index(name = "idx_community_uid_post_type_created", columnList = "uid, post_type, created_at"),
                @Index(name = "idx_community_post_type_like", columnList = "post_type, like_count"),
                @Index(name = "idx_community_post_type_view", columnList = "post_type, view_count"),
                @Index(name = "idx_community_post_type_comment", columnList = "post_type, comment_count")
        }
)
public class Community extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private CommunityTag communityTag;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "hash_tags", columnDefinition = "TEXT")
    private List<String> hashTags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", length = 30)
    private MarketTradeType tradeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_status", length = 30)
    private MarketStatus marketStatus;

    @Column(name = "price")
    private Long price;

    @Builder.Default
    @Column(name = "price_negotiable", nullable = false)
    private Boolean priceNegotiable = false;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_method", length = 30)
    private MarketTradeMethod tradeMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code")
    private LegalRegion region;

    @Builder.Default
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityPhoto> photos = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityLike> likes = new ArrayList<>();

    public void syncCounts(long viewCount, long likeCount) {
        this.viewCount = Math.max(0L, viewCount);
        this.likeCount = Math.max(0L, likeCount);
    }

    public void updateContent(
            CommunityTag tag,
            String title,
            String content,
            List<String> hashTags,
            MarketTradeType tradeType,
            MarketStatus marketStatus,
            Long price,
            Boolean priceNegotiable,
            LocalDate expiryDate,
            MarketTradeMethod tradeMethod,
            LegalRegion region
    ) {
        if (title == null || title.isBlank() || content == null || content.isBlank()) {
            throw GeneralException.of(CommunityErrorCode.POST_CONTENT_400);
        }
        if (tag == null) {
            throw GeneralException.of(CommunityErrorCode.TAG_NOT_FOUND_404);
        }
        this.communityTag = tag;
        this.title = title;
        this.content = content;
        this.hashTags = hashTags == null ? new ArrayList<>() : new ArrayList<>(hashTags);
        this.tradeType = tradeType;
        this.marketStatus = marketStatus;
        this.price = price;
        this.priceNegotiable = priceNegotiable != null && priceNegotiable;
        this.expiryDate = expiryDate;
        this.tradeMethod = tradeMethod;
        this.region = region;
    }

    public void replacePhotos(List<CommunityPhoto> nextPhotos) {
        this.photos.clear();
        if (nextPhotos == null || nextPhotos.isEmpty()) {
            return;
        }
        for (int i = 0; i < nextPhotos.size(); i++) {
            CommunityPhoto photo = nextPhotos.get(i);
            photo.bindCommunity(this);
            photo.changeSortOrder(i);
            this.photos.add(photo);
        }
        ensureSingleThumbnail(null);
    }

    public void ensureSingleThumbnail(String preferredUrl) {
        if (photos.isEmpty()) {
            return;
        }
        for (CommunityPhoto photo : photos) {
            photo.markThumbnail(false);
        }
        CommunityPhoto selected;
        if (preferredUrl != null && !preferredUrl.isBlank()) {
            selected = photos.stream()
                    .filter(p -> Objects.equals(p.getPhotoS3Url(), preferredUrl))
                    .findFirst()
                    .orElse(null);
            if (selected == null) {
                throw GeneralException.of(CommunityErrorCode.THUMBNAIL_INVALID_400);
            }
        } else {
            selected = photos.getFirst();
        }
        selected.markThumbnail(true);
    }

    public void applyThumbnailIndex(Integer thumbnailIndex) {
        if (photos.isEmpty()) {
            return;
        }
        int idx = thumbnailIndex == null ? 0 : thumbnailIndex;
        if (idx < 0 || idx >= photos.size()) {
            throw GeneralException.of(CommunityErrorCode.THUMBNAIL_INVALID_400);
        }
        for (CommunityPhoto photo : photos) {
            photo.markThumbnail(false);
        }
        photos.get(idx).markThumbnail(true);
    }

    public void increaseCommentCount() {
        this.commentCount = this.commentCount == null ? 1L : this.commentCount + 1L;
    }

    public void decreaseCommentCount() {
        if (this.commentCount == null || this.commentCount <= 0L) {
            this.commentCount = 0L;
            return;
        }
        this.commentCount = this.commentCount - 1L;
    }

    public String resolveThumbnailUrl() {
        return photos.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsThumbnail()))
                .map(CommunityPhoto::getPhotoS3Url)
                .findFirst()
                .orElseGet(() -> photos.stream()
                        .findFirst()
                        .map(CommunityPhoto::getPhotoS3Url)
                        .orElse(null));
    }
}

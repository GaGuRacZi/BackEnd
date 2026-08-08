package com.gaguraczi.paw.domain.community.dto.res;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeMethod;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "커뮤니티 게시글 상세")
public class CommunityDetailRes {

    @Schema(example = "10")
    private final Long postId;
    @Schema(example = "COMMUNICATION")
    private final PostType postType;
    @Schema(example = "건강상담")
    private final String tagName;
    @Schema(description = "태그 enum 코드", example = "HEALTH_CONSULT")
    private final CommunityTagCode tagCode;
    @Schema(example = "산책 후 발바닥이 빨개요")
    private final String title;
    @Schema(example = "병원 가봐야 할까요?")
    private final String content;
    @Schema(example = "[\"산책\", \"건강\"]")
    private final List<String> hashTags;
    private final List<CommunityPhotoRes> photos;
    @Schema(example = "13")
    private final Long viewCount;
    @Schema(example = "3")
    private final Long likeCount;
    @Schema(example = "2")
    private final Long commentCount;
    @Schema(description = "현재 로그인 유저 좋아요 여부", example = "false")
    private final Boolean likedByMe;
    @Schema(example = "길동이")
    private final String authorNickname;
    private final MarketTradeType tradeType;
    private final MarketStatus marketStatus;
    private final Long price;
    private final Boolean priceNegotiable;
    private final LocalDate expiryDate;
    private final MarketTradeMethod tradeMethod;
    private final String regionName;
    @Schema(example = "2026-08-08T10:00:00")
    private final LocalDateTime createdAt;

    public static CommunityDetailRes from(Community community, long viewCount, long likeCount, Boolean likedByMe) {
        List<CommunityPhotoRes> photos = community.getPhotos().stream()
                .sorted(Comparator.comparing(p -> p.getSortOrder() == null ? 0 : p.getSortOrder()))
                .map(CommunityPhotoRes::from)
                .toList();
        return CommunityDetailRes.builder()
                .postId(community.getPostId())
                .postType(community.getPostType())
                .tagName(community.getCommunityTag().getTagName())
                .tagCode(CommunityTagCode.valueOf(community.getCommunityTag().getTagCode()))
                .title(community.getTitle())
                .content(community.getContent())
                .hashTags(community.getHashTags())
                .photos(photos)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(community.getCommentCount())
                .likedByMe(likedByMe)
                .authorNickname(community.getUser().getNickname())
                .tradeType(community.getTradeType())
                .marketStatus(community.getMarketStatus())
                .price(community.getPrice())
                .priceNegotiable(community.getPriceNegotiable())
                .expiryDate(community.getExpiryDate())
                .tradeMethod(community.getTradeMethod())
                .regionName(community.getRegion() != null ? community.getRegion().getName() : null)
                .createdAt(community.getCreatedAt())
                .build();
    }
}

package com.gaguraczi.paw.domain.community.dto.res;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeMethod;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Schema(description = "커뮤니티 게시글 상세")
public record CommunityDetailRes(
        @Schema(example = "10") Long postId,
        @Schema(example = "COMMUNICATION") PostType postType,
        @Schema(example = "건강상담") String tagName,
        @Schema(description = "태그 enum 코드", example = "HEALTH_CONSULT") CommunityTagCode tagCode,
        @Schema(example = "산책 후 발바닥이 빨개요") String title,
        @Schema(example = "병원 가봐야 할까요?") String content,
        @Schema(example = "[\"산책\", \"건강\"]") List<String> hashTags,
        List<CommunityPhotoRes> photos,
        @Schema(example = "13") Long viewCount,
        @Schema(example = "3") Long likeCount,
        @Schema(example = "2") Long commentCount,
        @Schema(description = "현재 로그인 유저 좋아요 여부", example = "false") Boolean likedByMe,
        @Schema(example = "길동이") String authorNickname,
        MarketTradeType tradeType,
        MarketStatus marketStatus,
        Long price,
        Boolean priceNegotiable,
        LocalDate expiryDate,
        MarketTradeMethod tradeMethod,
        String regionName,
        @Schema(example = "2026-08-08T10:00:00") LocalDateTime createdAt
) {
    public static CommunityDetailRes from(Community community, long viewCount, long likeCount, Boolean likedByMe) {
        List<CommunityPhotoRes> photos = community.getPhotos().stream()
                .sorted(Comparator.comparing(p -> p.getSortOrder() == null ? 0 : p.getSortOrder()))
                .map(CommunityPhotoRes::from)
                .toList();
        return new CommunityDetailRes(
                community.getPostId(),
                community.getPostType(),
                community.getCommunityTag().getTagName(),
                parseTagCode(community.getCommunityTag().getTagCode()),
                community.getTitle(),
                community.getContent(),
                community.getHashTags(),
                photos,
                viewCount,
                likeCount,
                community.getCommentCount(),
                likedByMe,
                community.getUser().getNickname(),
                community.getTradeType(),
                community.getMarketStatus(),
                community.getPrice(),
                community.getPriceNegotiable(),
                community.getExpiryDate(),
                community.getTradeMethod(),
                community.getRegion() != null ? community.getRegion().getName() : null,
                community.getCreatedAt()
        );
    }

    private static CommunityTagCode parseTagCode(String tagCode) {
        if (tagCode == null || tagCode.isBlank()) {
            return null;
        }
        try {
            return CommunityTagCode.valueOf(tagCode);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

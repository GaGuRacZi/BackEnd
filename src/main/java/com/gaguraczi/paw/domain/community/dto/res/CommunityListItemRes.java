package com.gaguraczi.paw.domain.community.dto.res;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "커뮤니티 목록 아이템")
public class CommunityListItemRes {

    @Schema(example = "10")
    private final Long postId;
    @Schema(example = "MARKET")
    private final PostType postType;
    @Schema(example = "사료·간식")
    private final String tagName;
    @Schema(description = "태그 enum 코드", example = "FOOD_SNACK")
    private final CommunityTagCode tagCode;
    @Schema(example = "사료 나눔합니다")
    private final String title;
    @Schema(description = "본문 미리보기 (최대 80자)", example = "개봉만 했습니다.")
    private final String contentPreview;
    @Schema(example = "12")
    private final Long viewCount;
    @Schema(example = "3")
    private final Long likeCount;
    @Schema(example = "1")
    private final Long commentCount;
    @Schema(example = "길동이")
    private final String authorNickname;
    @Schema(example = "https://cdn.example.com/community/10/a.jpg")
    private final String thumbnailUrl;
    @Schema(example = "SHARE")
    private final MarketTradeType tradeType;
    @Schema(example = "IN_PROGRESS")
    private final MarketStatus marketStatus;
    private final Long price;
    @Schema(example = "false")
    private final Boolean priceNegotiable;
    @Schema(example = "서울특별시 강남구")
    private final String regionName;
    @Schema(example = "2026-08-08T10:00:00")
    private final LocalDateTime createdAt;

    public static CommunityListItemRes from(Community community, long viewCount, long likeCount) {
        String preview = community.getContent();
        if (preview != null && preview.length() > 80) {
            preview = preview.substring(0, 80);
        }
        String thumbnail = community.resolveThumbnailUrl();
        return CommunityListItemRes.builder()
                .postId(community.getPostId())
                .postType(community.getPostType())
                .tagName(community.getCommunityTag().getTagName())
                .tagCode(CommunityTagCode.valueOf(community.getCommunityTag().getTagCode()))
                .title(community.getTitle())
                .contentPreview(preview)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(community.getCommentCount())
                .authorNickname(community.getUser().getNickname())
                .thumbnailUrl(thumbnail)
                .tradeType(community.getTradeType())
                .marketStatus(community.getMarketStatus())
                .price(community.getPrice())
                .priceNegotiable(community.getPriceNegotiable())
                .regionName(community.getRegion() != null ? community.getRegion().getName() : null)
                .createdAt(community.getCreatedAt())
                .build();
    }
}

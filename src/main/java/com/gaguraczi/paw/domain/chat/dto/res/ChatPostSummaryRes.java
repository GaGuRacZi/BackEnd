package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 상단 장터 글 카드. postId로 매번 조회하므로 가격·거래상태가 최신입니다. 삭제돼도 대화는 유지됩니다.")
public record ChatPostSummaryRes(
        @Schema(description = "장터 글 ID", example = "33")
        Long postId,
        @Schema(description = "제목. deleted면 null", example = "사료 나눔합니다")
        String title,
        @Schema(description = "썸네일 URL. deleted면 null")
        String thumbnailUrl,
        @Schema(description = "가격. 나눔이면 0일 수 있음. deleted면 null", example = "0")
        Long price,
        @Schema(description = "가격 협의 가능. deleted면 null", example = "false")
        Boolean priceNegotiable,
        @Schema(description = "IN_PROGRESS | RESERVED | COMPLETED. deleted면 null", example = "IN_PROGRESS")
        MarketStatus marketStatus,
        @Schema(description = "글이 삭제되어 더 이상 조회되지 않으면 true. 카드는 '삭제된 게시글'로 그리세요", example = "false")
        boolean deleted
) {
    public static ChatPostSummaryRes from(Community community) {
        return new ChatPostSummaryRes(
                community.getPostId(),
                community.getTitle(),
                community.resolveThumbnailUrl(),
                community.getPrice(),
                community.getPriceNegotiable(),
                community.getMarketStatus(),
                false
        );
    }

    public static ChatPostSummaryRes deleted(Long postId) {
        return new ChatPostSummaryRes(postId, null, null, null, null, null, true);
    }
}

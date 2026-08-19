package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 상단에 노출되는 게시글 요약. postId로 매번 실시간 조회하므로 가격/거래상태가 최신값으로 반영된다.")
public record ChatPostSummaryRes(
        Long postId,
        @Schema(example = "사료 나눔합니다") String title,
        String thumbnailUrl,
        Long price,
        Boolean priceNegotiable,
        MarketStatus marketStatus,
        @Schema(description = "게시글이 삭제되어 더 이상 조회되지 않는 경우 true", example = "false") boolean deleted
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

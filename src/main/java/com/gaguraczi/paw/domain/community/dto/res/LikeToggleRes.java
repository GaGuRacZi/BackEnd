package com.gaguraczi.paw.domain.community.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "좋아요 토글 결과")
public class LikeToggleRes {

    @Schema(description = "토글 후 좋아요 여부", example = "true")
    private final boolean liked;
    @Schema(description = "토글 후 좋아요 수 (Redis 절대 카운트)", example = "4")
    private final long likeCount;

    public static LikeToggleRes of(boolean liked, long likeCount) {
        return LikeToggleRes.builder()
                .liked(liked)
                .likeCount(likeCount)
                .build();
    }
}

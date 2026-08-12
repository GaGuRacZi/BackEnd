package com.gaguraczi.paw.domain.community.dto.req;

import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketTradeMethod;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "커뮤니티 게시글 작성 요청 (multipart data part JSON)")
public record CommunityCreateReq(
        @NotNull
        @Schema(description = "게시글 유형 (COMMUNICATION | MARKET)", example = "COMMUNICATION", requiredMode = Schema.RequiredMode.REQUIRED)
        PostType postType,

        @NotNull
        @Schema(description = "태그 코드 (postType과 일치해야 함)", example = "HEALTH_CONSULT", requiredMode = Schema.RequiredMode.REQUIRED)
        CommunityTagCode tagCode,

        @NotBlank
        @Size(max = 200)
        @Schema(description = "제목 (최대 200자)", example = "산책 친구 구해요", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank
        @Schema(description = "본문", example = "주말에 같이 산책하실 분", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "해시태그", example = "[\"산책\", \"건강\"]")
        List<String> hashTags,

        @Schema(description = "신규 업로드 이미지 중 썸네일 인덱스 (0-based). 없으면 0", example = "0")
        Integer thumbnailIndex,

        @Schema(description = "장터 거래 유형 (MARKET 필수)", example = "SHARE")
        MarketTradeType tradeType,

        @Schema(description = "가격 (장터)", example = "10000")
        Long price,

        @Schema(description = "가격 협의 가능 여부", example = "false")
        Boolean priceNegotiable,

        @Schema(description = "나눔/거래 마감일 (yyyy-MM-dd)", example = "2026-09-01")
        LocalDate expiryDate,

        @Schema(description = "거래 방법 (MARKET 필수)", example = "DIRECT")
        MarketTradeMethod tradeMethod,

        @Schema(description = "법정동 코드 (선택, 있으면 유효해야 함)", example = "1168010100")
        String regionCode
) {
}

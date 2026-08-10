package com.gaguraczi.paw.domain.community.dto.req;

import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeMethod;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "커뮤니티 게시글 수정 요청 (multipart data part JSON). postType은 수정 불가.")
public record CommunityUpdateReq(
        @NotNull
        @Schema(description = "태그 코드 (게시글 postType과 일치해야 함)", example = "FOOD_SNACK", requiredMode = Schema.RequiredMode.REQUIRED)
        CommunityTagCode tagCode,

        @NotBlank
        @Size(max = 200)
        @Schema(description = "제목 (최대 200자)", example = "사료 나눔 (예약중)", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank
        @Schema(description = "본문", example = "개봉만 했습니다. 내일까지 가능해요.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "해시태그", example = "[\"나눔\"]")
        List<String> hashTags,

        @Schema(description = "유지할 기존 사진 URL 목록. 목록에 없는 기존 사진은 삭제됨")
        List<String> keepPhotoUrls,

        @Schema(description = "최종 썸네일로 쓸 기존 사진 URL", example = "https://cdn.example.com/community/10/a.jpg")
        String thumbnailUrl,

        @Schema(description = "thumbnailUrl이 없을 때, keep+신규 합친 목록 기준 썸네일 인덱스", example = "0")
        Integer thumbnailIndex,

        @Schema(description = "장터 거래 유형 (MARKET 필수)", example = "SHARE")
        MarketTradeType tradeType,

        @Schema(description = "장터 상태", example = "RESERVED")
        MarketStatus marketStatus,

        @Schema(description = "가격", example = "10000")
        Long price,

        @Schema(description = "가격 협의 가능 여부", example = "false")
        Boolean priceNegotiable,

        @Schema(description = "나눔/거래 마감일", example = "2026-09-01")
        LocalDate expiryDate,

        @Schema(description = "거래 방법", example = "DIRECT")
        MarketTradeMethod tradeMethod,

        @Schema(description = "법정동 코드", example = "1168010100")
        String regionCode
) {
}

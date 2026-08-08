package com.gaguraczi.paw.domain.community.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = """
        커뮤니티 태그 코드.
        글쓰기/수정/목록 필터에서 tagId 대신 사용합니다.
        postType과 일치해야 합니다.
        """)
public enum CommunityTagCode {

    // COMMUNICATION
    @Schema(description = "건강상담")
    HEALTH_CONSULT(PostType.COMMUNICATION, "건강상담", 1),
    @Schema(description = "산책친구")
    WALK_BUDDY(PostType.COMMUNICATION, "산책친구", 2),
    @Schema(description = "헌혈소식")
    BLOOD_NEWS(PostType.COMMUNICATION, "헌혈소식", 3),
    @Schema(description = "동네정보")
    LOCAL_INFO(PostType.COMMUNICATION, "동네정보", 4),

    // MARKET
    @Schema(description = "사료·간식")
    FOOD_SNACK(PostType.MARKET, "사료·간식", 1),
    @Schema(description = "용품")
    SUPPLIES(PostType.MARKET, "용품", 2),
    @Schema(description = "소모품")
    CONSUMABLES(PostType.MARKET, "소모품", 3),
    @Schema(description = "영양제")
    SUPPLEMENT(PostType.MARKET, "영양제", 4),
    @Schema(description = "기타")
    OTHER(PostType.MARKET, "기타", 5),

    // REVIEW (시드만, 피드 API 미지원)
    @Schema(description = "산책 장소")
    WALK_PLACE(PostType.REVIEW, "산책 장소", 1),
    @Schema(description = "병원")
    HOSPITAL(PostType.REVIEW, "병원", 2),
    @Schema(description = "용품샵")
    SUPPLY_SHOP(PostType.REVIEW, "용품샵", 3),
    @Schema(description = "미용실")
    GROOMING(PostType.REVIEW, "미용실", 4);

    private final PostType postType;
    private final String tagName;
    private final int sortOrder;
}

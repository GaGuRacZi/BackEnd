package com.gaguraczi.paw.domain.community.dto.res;

import com.gaguraczi.paw.domain.community.entity.CommunityTag;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "커뮤니티 태그(칩). 글쓰기/필터에는 tagCode를 사용합니다.")
public class CommunityTagRes {

    @Schema(example = "건강상담")
    private final String tagName;
    @Schema(description = "태그 enum 코드", example = "HEALTH_CONSULT")
    private final CommunityTagCode tagCode;
    @Schema(example = "COMMUNICATION")
    private final PostType postType;
    @Schema(example = "1")
    private final Integer sortOrder;

    public static CommunityTagRes from(CommunityTag tag) {
        return CommunityTagRes.builder()
                .tagName(tag.getTagName())
                .tagCode(CommunityTagCode.valueOf(tag.getTagCode()))
                .postType(tag.getPostType())
                .sortOrder(tag.getSortOrder())
                .build();
    }
}

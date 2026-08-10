package com.gaguraczi.paw.domain.community.dto.req;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "CommunityUpdateMultipart", description = "커뮤니티 게시글 수정 multipart")
public class CommunityUpdateMultipart {

    @Schema(
            description = "게시글 수정 JSON (CommunityUpdateReq)",
            implementation = CommunityUpdateReq.class,
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = """
                    {
                      "tagCode": "FOOD_SNACK",
                      "title": "사료 나눔 (예약중)",
                      "content": "개봉만 했습니다. 내일까지 가능해요.",
                      "hashTags": ["나눔"],
                      "keepPhotoUrls": ["https://cdn.example.com/community/10/a.jpg"],
                      "thumbnailUrl": "https://cdn.example.com/community/10/a.jpg",
                      "tradeType": "SHARE",
                      "marketStatus": "RESERVED",
                      "tradeMethod": "DIRECT",
                      "regionCode": "1168010100"
                    }
                    """
    )
    public CommunityUpdateReq data;

    @Schema(description = "신규 이미지")
    @ArraySchema(maxItems = 5, schema = @Schema(type = "string", format = "binary"))
    public MultipartFile[] images;
}

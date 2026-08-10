package com.gaguraczi.paw.domain.community.dto.req;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "CommunityCreateMultipart", description = "커뮤니티 게시글 작성 multipart")
public class CommunityCreateMultipart {

    @Schema(
            description = "게시글 JSON (CommunityCreateReq)",
            implementation = CommunityCreateReq.class,
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = """
                    {
                      "postType": "COMMUNICATION",
                      "tagCode": "HEALTH_CONSULT",
                      "title": "산책 친구 구해요",
                      "content": "주말에 같이 산책하실 분",
                      "hashTags": ["산책"],
                      "thumbnailIndex": 0
                    }
                    """
    )
    public CommunityCreateReq data;

    @Schema(description = "이미지 0~5장")
    @ArraySchema(maxItems = 5, schema = @Schema(type = "string", format = "binary"))
    public MultipartFile[] images;
}

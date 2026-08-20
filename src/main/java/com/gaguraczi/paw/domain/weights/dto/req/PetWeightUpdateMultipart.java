package com.gaguraczi.paw.domain.weights.dto.req;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "PetWeightUpdateMultipart", description = "체중 기록 수정 multipart")
public class PetWeightUpdateMultipart {

    @Schema(
            description = "체중 기록 JSON (PetWeightUpdateReq, 보낸 값만 반영). 생략 가능(사진만 수정할 때).",
            implementation = PetWeightUpdateReq.class,
            example = """
                    {
                      "weight": 4.30,
                      "memoContent": "산책 직후 측정",
                      "keepPhotoUrls": ["https://cdn.example.com/pet-weight/1/a.jpg"]
                    }
                    """
    )
    public PetWeightUpdateReq data;

    @Schema(description = "신규로 추가할 메모 사진 (기존 사진 + 신규 사진 합이 최대 3장)")
    @ArraySchema(maxItems = 3, schema = @Schema(type = "string", format = "binary"))
    public MultipartFile[] images;
}

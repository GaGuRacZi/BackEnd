package com.gaguraczi.paw.domain.weights.dto.req;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "PetWeightCreateMultipart", description = "체중 기록 저장 multipart")
public class PetWeightCreateMultipart {

    @Schema(
            description = "체중 기록 JSON (PetWeightCreateReq)",
            implementation = PetWeightCreateReq.class,
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = """
                    {
                      "weight": 4.20,
                      "bodyType": "HEALTHY",
                      "appetiteType": "LOW",
                      "memoContent": "식사 후 같은 시간대에 측정했어요.",
                      "recordedAt": "2026-07-06T20:30:00"
                    }
                    """
    )
    public PetWeightCreateReq data;

    @Schema(description = "메모 사진 0~3장")
    @ArraySchema(maxItems = 3, schema = @Schema(type = "string", format = "binary"))
    public MultipartFile[] images;
}

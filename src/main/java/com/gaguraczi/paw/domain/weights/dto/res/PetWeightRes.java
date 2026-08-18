package com.gaguraczi.paw.domain.weights.dto.res;

import com.gaguraczi.paw.domain.weights.entity.PetWeightEntity;
import com.gaguraczi.paw.domain.weights.enums.AppetiteTypeEnum;
import com.gaguraczi.paw.domain.weights.enums.BodyTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "PetWeightRes", description = "체중 기록 단건 응답")
public record PetWeightRes(

        @Schema(description = "체중 기록 ID", example = "1")
        Long petWeightId,

        @Schema(description = "반려동물 ID", example = "1")
        Long petId,

        @Schema(description = "몸무게(kg)", example = "4.20")
        BigDecimal weight,

        @Schema(description = "체형 상태", example = "HEALTHY")
        BodyTypeEnum bodyType,

        @Schema(description = "컨디션(식욕) 체크", example = "LOW")
        AppetiteTypeEnum appetiteType,

        @Schema(description = "메모", example = "식사 후 같은 시간대에 측정했어요.")
        String memoContent,

        @Schema(description = "기록일자", example = "2026-07-06T20:30:00")
        LocalDateTime recordedAt
) {

    public static PetWeightRes from(PetWeightEntity petWeight) {
        return new PetWeightRes(
                petWeight.getPetWeightId(),
                petWeight.getPet().getPetId(),
                petWeight.getWeight(),
                petWeight.getBodyType(),
                petWeight.getAppetiteType(),
                petWeight.getMemoContent(),
                petWeight.getRecordedAt()
        );
    }
}

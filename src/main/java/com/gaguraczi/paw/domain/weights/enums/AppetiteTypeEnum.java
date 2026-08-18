package com.gaguraczi.paw.domain.weights.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "컨디션(식욕) 체크")
public enum AppetiteTypeEnum {

    @Schema(description = "식욕이 떨어짐")
    LOW,

    @Schema(description = "식욕 평범")
    MIDDLE,

    @Schema(description = "식욕이 많음")
    HIGH
}

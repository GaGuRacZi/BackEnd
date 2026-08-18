package com.gaguraczi.paw.domain.weights.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체형 상태")
public enum BodyTypeEnum {

    @Schema(description = "마름")
    SKINNY,

    @Schema(description = "적정")
    HEALTHY,

    @Schema(description = "과체중")
    OVER_WEIGHT
}

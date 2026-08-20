package com.gaguraczi.paw.domain.weights.enums;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "체형. 요청/응답은 영문 enum. SKINNY=마름, HEALTHY=적정, OVER_WEIGHT=과체중")
public enum BodyTypeEnum {

    @Schema(description = "마름")
    SKINNY("마름"),
    @Schema(description = "적정")
    HEALTHY("적정"),
    @Schema(description = "과체중")
    OVER_WEIGHT("과체중");

    private final String label;
}
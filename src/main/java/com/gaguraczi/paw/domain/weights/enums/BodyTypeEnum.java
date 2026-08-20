package com.gaguraczi.paw.domain.weights.enums;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "체형 상태 (SKINNY: 마름, HEALTHY: 적정, OVER_WEIGHT: 과체중)")
public enum BodyTypeEnum {

    SKINNY("마름"),
    HEALTHY("적정"),
    OVER_WEIGHT("과체중");

    private final String label;
}
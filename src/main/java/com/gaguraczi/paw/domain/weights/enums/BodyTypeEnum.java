package com.gaguraczi.paw.domain.weights.enums;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "체형 상태 (Skinny: 마름, Healthy: 적정, Over_weight: 과체중)")
public enum BodyTypeEnum {

    Skinny("마름"),
    Healthy("적정"),
    Over_weight("과체중");

    private final String label;
}
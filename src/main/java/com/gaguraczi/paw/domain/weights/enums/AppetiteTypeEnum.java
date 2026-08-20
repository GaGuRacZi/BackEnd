package com.gaguraczi.paw.domain.weights.enums;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "식욕 상태 (Low: 식욕이 떨어짐, Middle: 식욕 평범, High: 식욕이 많음)")
public enum AppetiteTypeEnum {

    Low("식욕이 떨어짐"),
    Middle("식욕 평범"),
    High("식욕이 많음");

    private final String label;
}
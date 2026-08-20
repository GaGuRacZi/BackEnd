package com.gaguraczi.paw.domain.walk.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.gaguraczi.paw.domain.walk.exception.WalkErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.Arrays;


@AllArgsConstructor
@Schema(description = "날씨 (한글로 주고받습니다)",
        example = "맑음",
        allowableValues = {"맑음", "흐림", "비", "눈", "바람"})
public enum WeatherTypeEnum {

    SUNNY("맑음"),
    CLOUDY("흐림"),
    RAINY("비"),
    SNOWY("눈"),
    WINDY("바람");

    private final String description;


    @JsonValue
    public String getDescription() {
        return description;
    }

    public static WeatherTypeEnum from(String value) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(WalkErrorCode.WALK_WEATHER_INVALID);
        }
        String target = value.trim();

        return Arrays.stream(values())
                .filter(type -> type.description.equals(target) || type.name().equalsIgnoreCase(target))
                .findFirst()
                .orElseThrow(() -> new GeneralException(WalkErrorCode.WALK_WEATHER_INVALID));
    }


    public static WeatherTypeEnum fromNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return from(value);
    }
}

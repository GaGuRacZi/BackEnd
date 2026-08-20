package com.gaguraczi.paw.domain.walk.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.gaguraczi.paw.domain.walk.exception.WalkErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.Arrays;

// 산책 강도
@AllArgsConstructor
@Schema(description = "산책 강도 (한글로 주고받습니다)",
        example = "보통",
        allowableValues = {"느긋", "보통", "활발"})
public enum WalkTypeEnum {

    EASY("느긋"),
    NORMAL("보통"),
    HARD("활발");

    private final String description;


    @JsonValue
    public String getDescription() {
        return description;
    }


    public static WalkTypeEnum from(String value) {
        if (value == null || value.isBlank()) {
            throw new GeneralException(WalkErrorCode.WALK_TYPE_INVALID);
        }
        String target = value.trim();

        return Arrays.stream(values())
                .filter(type -> type.description.equals(target) || type.name().equalsIgnoreCase(target))
                .findFirst()
                .orElseThrow(() -> new GeneralException(WalkErrorCode.WALK_TYPE_INVALID));
    }


    public static WalkTypeEnum fromNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return from(value);
    }
}

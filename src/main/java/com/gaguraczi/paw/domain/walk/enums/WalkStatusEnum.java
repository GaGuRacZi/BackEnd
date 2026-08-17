package com.gaguraczi.paw.domain.walk.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

//산책 진행중
@Getter
@AllArgsConstructor
@Schema(description = "산책 진행 상태", example = "COMPLETED")
public enum WalkStatusEnum {

    IN_PROGRESS("진행중"),
    COMPLETED("완료");

    private final String description;
}

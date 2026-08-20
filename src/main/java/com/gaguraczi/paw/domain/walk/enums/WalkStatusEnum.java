package com.gaguraczi.paw.domain.walk.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

//산책 진행중
@Getter
@AllArgsConstructor
@Schema(description = "산책 진행 상태. IN_PROGRESS=타이머 중(walkId 없음), COMPLETED=DB 저장 완료",
        example = "COMPLETED", allowableValues = {"IN_PROGRESS", "COMPLETED"})
public enum WalkStatusEnum {

    @Schema(description = "타이머 진행 중. Redis에만 있고 DB 미저장")
    IN_PROGRESS("진행중"),
    @Schema(description = "산책 완료. walkId로 상세/수정/삭제 가능")
    COMPLETED("완료");

    private final String description;
}

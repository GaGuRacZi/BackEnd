package com.gaguraczi.paw.domain.visit.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                하루 복용 횟수. 처방 추가 시 필수입니다.
                - ONCE_DAILY: 1일 1회
                - TWICE_DAILY: 1일 2회
                - THREE_TIMES: 1일 3회
                - AS_NEEDED: 필요 시
                """
)
public enum DoseFrequency {
    ONCE_DAILY,
    TWICE_DAILY,
    THREE_TIMES,
    AS_NEEDED
}

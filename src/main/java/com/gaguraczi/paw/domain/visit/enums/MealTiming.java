package com.gaguraczi.paw.domain.visit.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                식사 기준 복용 시점. 처방 추가 시 필수입니다.
                - BEFORE_MEAL: 식전
                - AFTER_MEAL: 식후
                - BETWEEN_MEALS: 식간
                - ANYTIME: 상관없음
                """
)
public enum MealTiming {
    BEFORE_MEAL,
    AFTER_MEAL,
    BETWEEN_MEALS,
    ANYTIME
}

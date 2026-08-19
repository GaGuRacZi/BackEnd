package com.gaguraczi.paw.domain.visit.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                복용 시간대. 여러 개 선택할 수 있으며 생략하면 빈 배열로 저장됩니다.
                - MORNING: 아침
                - LUNCH: 점심
                - EVENING: 저녁
                - BEDTIME: 취침 전
                """
)
public enum TakeTime {
    MORNING,
    LUNCH,
    EVENING,
    BEDTIME
}

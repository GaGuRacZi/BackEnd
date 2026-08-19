package com.gaguraczi.paw.domain.visit.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                코인을 쓰는 AI 상세 요약(마크다운) 상태. 짧은 요약(oneLineSummary)과는 별개입니다.
                - NONE: 아직 생성하지 않음. POST /visits/{visitId}/ai-summary 로 생성합니다.
                - GENERATING: 생성 중. 같은 진료에 대한 동시 요청은 VISIT_409.
                - DONE: 생성 완료. 이후 같은 POST는 재과금 없이 저장된 마크다운을 반환합니다.
                """
)
public enum AiSummaryStatus {
    NONE,
    GENERATING,
    DONE
}

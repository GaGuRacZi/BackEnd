package com.gaguraczi.paw.domain.visit.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                전사문 화자. STT 화자 분리 후 서버가 의사/보호자로 매핑합니다.
                - VET: 수의사
                - OWNER: 보호자
                """
)
public enum TranscriptSpeaker {
    VET,
    OWNER
}

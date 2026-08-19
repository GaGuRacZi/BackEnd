package com.gaguraczi.paw.domain.visit.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                처방 약물 출처.
                - CATALOG: GET /medications 검색 결과의 medicationId로 마스터 약을 선택. nameKo/nameEn/ingredient는 마스터에서 채웁니다.
                - CUSTOM: 마스터에 없는 약. nameKo 필수, nameEn·ingredient는 선택.
                """
)
public enum PrescriptionSource {
    CATALOG,
    CUSTOM
}

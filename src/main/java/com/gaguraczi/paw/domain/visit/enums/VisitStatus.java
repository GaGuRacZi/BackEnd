package com.gaguraczi.paw.domain.visit.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                진료 녹음 처리 상태.
                - PROCESSING: 업로드 직후. STT·짧은 요약이 비동기로 돌아가는 중. 목록/상세의 visitName·oneLineSummary·진단/케어 본문은 null 또는 빈 배열.
                - READY: 전사문·짧은 요약 완료. 전사문 조회, 처방 추가, AI 상세 요약이 가능해집니다.
                - FAILED: STT 또는 짧은 요약 실패. 상세의 failReason을 확인하세요. 재처리 API는 없고 녹음을 다시 업로드해야 합니다.
                """
)
public enum VisitStatus {
    PROCESSING,
    READY,
    FAILED
}

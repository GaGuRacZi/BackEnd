package com.gaguraczi.paw.domain.visit.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VisitErrorCode implements BaseErrorCode {

    VISIT_AUDIO_REQUIRED(HttpStatus.BAD_REQUEST, "VISIT_400", "음성 파일이 필요합니다."),
    VISIT_AUDIO_TYPE(HttpStatus.BAD_REQUEST, "VISIT_400_AUDIO_TYPE", "mp3, m4a, aac 파일만 업로드할 수 있습니다."),
    VISIT_AUDIO_DURATION(HttpStatus.BAD_REQUEST, "VISIT_400_AUDIO_DURATION", "녹음은 최대 60분까지 가능합니다."),
    VISIT_AUDIO_TOO_LARGE(HttpStatus.BAD_REQUEST, "VISIT_400_AUDIO_TOO_LARGE", "음성 파일은 100MB 이하여야 합니다."),
    VISIT_PET_REQUIRED(HttpStatus.BAD_REQUEST, "VISIT_400_PET", "펫 ID가 필요합니다."),
    VISIT_NOT_READY(HttpStatus.BAD_REQUEST, "VISIT_400_NOT_READY", "진료 요약이 아직 준비되지 않았습니다."),
    VISIT_PRESCRIPTION_INVALID(HttpStatus.BAD_REQUEST, "VISIT_400_PRESCRIPTION", "약물 정보가 올바르지 않습니다."),
    VISIT_COIN_INSUFFICIENT(HttpStatus.PAYMENT_REQUIRED, "VISIT_402_COIN", "코인이 부족합니다."),
    VISIT_NOT_FOUND(HttpStatus.NOT_FOUND, "VISIT_404", "진료 기록을 찾을 수 없습니다."),
    VISIT_AI_SUMMARY_CONFLICT(HttpStatus.CONFLICT, "VISIT_409", "AI 요약을 생성 중입니다."),
    VISIT_STT_FAILED(HttpStatus.BAD_GATEWAY, "VISIT_502", "음성 전사에 실패했습니다."),
    VISIT_SUMMARY_FAILED(HttpStatus.BAD_GATEWAY, "VISIT_502_1", "진료 요약 생성에 실패했습니다."),
    VISIT_AI_SUMMARY_FAILED(HttpStatus.BAD_GATEWAY, "VISIT_502_2", "AI 진료 요약 생성에 실패했습니다."),
    VISIT_VECTOR_STORE_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "VISIT_503",
            "지식 검색 서비스를 일시적으로 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

package com.gaguraczi.paw.domain.location.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LocationErrorCode implements BaseErrorCode {

    LOCATION_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "LOCATION_400_1", "잘못된 위치 요청입니다."),
    LOCATION_API_KEY_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "LOCATION_500_1", "지도 API 키가 설정되지 않았습니다."),
    LOCATION_GEOCODE_API_FAILED(HttpStatus.BAD_GATEWAY, "LOCATION_502_1", "지오코딩 API 호출에 실패했습니다."),
    LOCATION_REVERSE_GEOCODE_API_FAILED(HttpStatus.BAD_GATEWAY, "LOCATION_502_2", "리버스 지오코딩 API 호출에 실패했습니다."),
    LOCATION_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "LOCATION_502_3", "지도 API 응답이 비어 있습니다."),
    LOCATION_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION_404_1", "주소 결과를 찾을 수 없습니다."),
    LOCATION_LEGAL_DISTRICT_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION_404_2", "법정동 코드 결과를 찾을 수 없습니다."),
    LOCATION_USER_NOT_SET(HttpStatus.NOT_FOUND, "LOCATION_404_3", "등록된 위치가 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

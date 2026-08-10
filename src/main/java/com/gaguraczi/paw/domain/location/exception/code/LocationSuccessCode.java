package com.gaguraczi.paw.domain.location.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LocationSuccessCode implements BaseSuccessCode {

    LOCATION_USER_GET_200(HttpStatus.OK, "LOCATION_200_1", "내 위치 조회에 성공했습니다."),
    LOCATION_USER_CERT_200(HttpStatus.OK, "LOCATION_200_2", "위치 인증에 성공했습니다."),
    LOCATION_ADDRESS_200(HttpStatus.OK, "LOCATION_200_3", "도로명 주소 조회에 성공했습니다."),
    LOCATION_RESOLVE_200(HttpStatus.OK, "LOCATION_200_4", "좌표 기준 지역/주소 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

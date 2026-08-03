package com.gaguraczi.paw.domain.region.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RegionErrorCode implements BaseErrorCode {

    REGION_NOT_FOUND(HttpStatus.BAD_REQUEST, "REGION_400", "유효하지 않은 지역 코드입니다."),
    REGION_ABOLISHED(HttpStatus.BAD_REQUEST, "REGION_400_1", "폐지된 지역입니다."),
    REGION_LEVEL_INVALID(HttpStatus.BAD_REQUEST, "REGION_400_2", "시/군/구 단위 지역만 선택할 수 있습니다."),
    REGION_QUERY_REQUIRED(HttpStatus.BAD_REQUEST, "REGION_400_3", "검색어를 입력해 주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

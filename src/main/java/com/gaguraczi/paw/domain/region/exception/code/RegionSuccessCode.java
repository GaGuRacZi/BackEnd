package com.gaguraczi.paw.domain.region.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RegionSuccessCode implements BaseSuccessCode {

    REGION_SEARCH_200(HttpStatus.OK, "REGION_SEARCH_200", "지역 검색에 성공했습니다."),
    REGION_SYNC_200(HttpStatus.OK, "REGION_SYNC_200", "법정동 동기화에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

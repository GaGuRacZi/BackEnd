package com.gaguraczi.paw.global.api.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {

    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
    
    default ReasonDTO getReason() {
        return ReasonDTO.builder()
                .httpStatus(getHttpStatus())
                .code(getCode())
                .message(getMessage())
                .build();
    }
}

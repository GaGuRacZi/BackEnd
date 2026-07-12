package com.gaguraczi.paw.domain.auth.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

public class AuthException extends GeneralException {

    public AuthException(BaseErrorCode code) {
        super(code);
    }

    public static AuthException of(BaseErrorCode code) {
        return new AuthException(code);
    }
}

package com.gaguraczi.paw.domain.auth.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

public class AuthException extends GeneralException {

    /**
     * Creates an authentication exception with the specified error code.
     *
     * @param code the error code describing the authentication failure
     */
    public AuthException(BaseErrorCode code) {
        super(code);
    }

    /**
     * Creates an authentication exception for the specified error code.
     *
     * @param code the error code associated with the exception
     * @return an authentication exception containing the specified error code
     */
    public static AuthException of(BaseErrorCode code) {
        return new AuthException(code);
    }
}

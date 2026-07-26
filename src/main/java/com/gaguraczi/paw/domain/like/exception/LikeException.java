package com.gaguraczi.paw.domain.like.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

public class LikeException extends GeneralException {

    public LikeException(BaseErrorCode code) {
        super(code);
    }

    public static LikeException of(BaseErrorCode code) {
        return new LikeException(code);
    }
}

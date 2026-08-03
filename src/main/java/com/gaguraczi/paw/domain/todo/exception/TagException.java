package com.gaguraczi.paw.domain.todo.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

public class TagException extends GeneralException {

    public TagException(BaseErrorCode code) {
        super(code);
    }

    public static TagException of(BaseErrorCode code) {
        return new TagException(code);
    }
}
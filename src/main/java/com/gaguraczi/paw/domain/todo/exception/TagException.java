package com.gaguraczi.paw.domain.todo.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

public class TagException extends GeneralException {

    /**
     * Creates a tag exception with the specified error code.
     *
     * @param code the error code describing the tag-related failure
     */
    public TagException(BaseErrorCode code) {
        super(code);
    }

    /**
     * Creates a tag exception for the specified error code.
     *
     * @param code the error code associated with the exception
     * @return a tag exception containing the specified error code
     */
    public static TagException of(BaseErrorCode code) {
        return new TagException(code);
    }
}
package com.gaguraczi.paw.utils.exception;

import lombok.Getter;

@Getter
public class UtilException extends RuntimeException {

    public enum Reason {
        FILE_EMPTY,
        FILE_TOO_LARGE,
        TYPE_NOT_ALLOWED,

        IMAGE_PROCESS_FAILED,

        S3_UPLOAD_FAILED,
        S3_DELETE_FAILED
    }

    private final Reason reason;

    public UtilException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public UtilException(Reason reason, Throwable cause) {
        super(reason.name(), cause);
        this.reason = reason;
    }
}
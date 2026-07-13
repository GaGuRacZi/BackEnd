package com.gaguraczi.paw.global.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import com.gaguraczi.paw.global.api.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 공통 예외 처리
@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

  private final BaseErrorCode code;

  /**
   * Creates an exception associated with the specified error code.
   *
   * @param code the error code associated with the exception
   * @return a new exception containing the specified error code
   */
  public static GeneralException of(BaseErrorCode code) {
    return new GeneralException(code);
  }

  /**
   * Provides detailed information about the exception's error code.
   *
   * @return the reason associated with the error code
   */
  public ReasonDTO getReason() {
    return this.code.getReason();
  }
}

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

  //예외 생성
  public static GeneralException of(BaseErrorCode code) {
    return new GeneralException(code);
  }

  //예외 상세 정보
  public ReasonDTO getReason() {
    return this.code.getReason();
  }
}

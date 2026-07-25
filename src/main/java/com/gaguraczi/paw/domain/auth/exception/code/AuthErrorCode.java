package com.gaguraczi.paw.domain.auth.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    NAVER_LOGIN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "NAVER_LOGIN_401", "유효하지 않은 값이 존재합니다(만료된 인가코드 등)."),
    NAVER_API_FAILED(HttpStatus.BAD_GATEWAY, "NAVER_502_1", "네이버 서버 응답에 실패했습니다."),

    KAKAO_LOGIN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "KAKAO_LOGIN_401", "유효하지 않은 카카오 토큰입니다."),
    KAKAO_API_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_502_1", "카카오 서버 응답에 실패했습니다."),

    // 로컬 회원가입
    LOCAL_SIGNUP_400_1(HttpStatus.BAD_REQUEST, "LOCAL_SIGNUP_400_1", "유효하지 않은 값이 존재합니다."),
    LOCAL_SIGNUP_409_1(HttpStatus.CONFLICT, "LOCAL_SIGNUP_409_1", "이미 존재하는 아이디입니다."),

    // 로컬 로그인
    LOCAL_LOGIN_400_1(HttpStatus.BAD_REQUEST, "LOCAL_LOGIN_400_1", "유효하지 않은 값이 존재합니다."),
    LOCAL_LOGIN_401_1(HttpStatus.UNAUTHORIZED, "LOCAL_LOGIN_401_1", "유효하지 않은 값이 존재합니다(만료된 인가코드 등)."),
    LOCAL_LOGIN_401_2(HttpStatus.UNAUTHORIZED, "LOCAL_LOGIN_401_2", "아이디 또는 비밀번호가 올바르지 않습니다."),
    NICKNAME_400(HttpStatus.BAD_REQUEST, "NICKNAME_400", "닉네임은 15자 이내의 영문, 숫자, 한글만 사용 가능합니다."),
    NICKNAME_409(HttpStatus.CONFLICT, "NICKNAME_409", "이미 사용 중인 닉네임입니다."),
    USERNAME_400(HttpStatus.BAD_REQUEST, "USERNAME_400", "아이디는 100자 이내이어야 합니다."),
    USERNAME_409(HttpStatus.CONFLICT, "USERNAME_409", "이미 사용 중인 아이디 입니다."),
    TERM_400(HttpStatus.BAD_REQUEST, "TERM_400", "필수 약관(SERVICE, PRIVACY)에 모두 동의해야 합니다."),

    // 이메일 인증
    EMAIL_SEND_400(HttpStatus.BAD_REQUEST, "EMAIL_SEND_400", "인증번호 전송에 실패했습니다."),
    EMAIL_SEND_429(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_SEND_429", "인증번호 재전송은 잠시 후 다시 시도해 주세요."),
    EMAIL_CODE_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_CODE_400", "인증번호가 일치하지 않거나 만료되었습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "이메일 인증이 필요합니다."),

    // 전화번호 인증
    PHONE_SEND_400_1(HttpStatus.BAD_REQUEST, "PHONE_SEND_400_1", "전화번호를 올바른 양식으로 작성해 주세요."),
    PHONE_SEND_400_2(HttpStatus.BAD_REQUEST, "PHONE_SEND_400_2", "인증번호 전송에 실패했습니다."),
    PHONE_CODE_INVALID(HttpStatus.BAD_REQUEST, "PHONE_CODE_400", "인증번호가 일치하지 않거나 만료되었습니다."),
    PHONE_400(HttpStatus.BAD_REQUEST, "PHONE_400", "중복된 전화번호입니다."),

    // 로그인 연동
    LOGIN_LINK_400(HttpStatus.BAD_REQUEST, "LOGIN_LINK_400", "로그인 연동 실패"),
    LOGIN_LINK_400_1(HttpStatus.BAD_REQUEST, "LOGIN_LINK_400_1", "해당 전화번호로 가입된 기존 계정이 없습니다."),
    LOGIN_LINK_400_2(HttpStatus.BAD_REQUEST, "LOGIN_LINK_400_2", "연동 대상이 현재 계정과 동일합니다."),
    LOGIN_LINK_400_3(HttpStatus.BAD_REQUEST, "LOGIN_LINK_400_3", "해당 소셜 타입은 이미 연동되어 있습니다."),
    LOGIN_LINK_USE_LOCAL(HttpStatus.CONFLICT, "LOGIN_LINK_USE_LOCAL", "로컬로 가입된 이메일입니다. 로컬 로그인 후 소셜 계정을 연동해 주세요."),
    LOGIN_LINK_USE_SOCIAL(HttpStatus.CONFLICT, "LOGIN_LINK_USE_SOCIAL", "소셜로 가입된 이메일입니다. 소셜 로그인 후 로컬 계정을 연동해 주세요."),
    LOGIN_LINK_EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "LOGIN_LINK_EMAIL_MISMATCH", "연동하려는 계정의 이메일이 일치하지 않습니다."),
    LOGIN_LINK_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "LOGIN_LINK_EMAIL_REQUIRED", "로컬 연동을 위해 계정 이메일이 필요합니다."),
    ONBOARDING_EMAIL_409(HttpStatus.CONFLICT, "ONBOARDING_EMAIL_409", "이미 이메일이 등록된 계정입니다."),
    ONBOARDING_400(HttpStatus.BAD_REQUEST, "ONBOARDING_400", "온보딩 실패"),
    ONBOARDING_500(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "ONBOARDING_500",
            "필수 컬렉션 데이터가 설정되지 않았습니다."),

    REFRESH_INVALID(HttpStatus.UNAUTHORIZED, "REFRESH_401", "토큰 재발급에 실패했습니다."),
    LOGOUT_INVALID(HttpStatus.UNAUTHORIZED, "LOGOUT_401", "유효하지 않은 토큰입니다."),
    SIGNOUT_INVALID(HttpStatus.UNAUTHORIZED, "SIGNOUT_401", "유효하지 않은 토큰입니다."),
    SIGNOUT_400(HttpStatus.BAD_REQUEST, "SIGNOUT_400", "회원탈퇴가 불가합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

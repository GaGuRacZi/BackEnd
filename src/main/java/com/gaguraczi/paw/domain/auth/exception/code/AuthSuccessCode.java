package com.gaguraczi.paw.domain.auth.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {

    // 네이버 앱 로그인 성공 코드
    NAVER_LOGIN_200_1(HttpStatus.OK, "NAVER_LOGIN_200_1", "처음으로 네이버 로그인에 성공했습니다."),
    NAVER_LOGIN_200_2(HttpStatus.OK, "NAVER_LOGIN_200_2", "로그인에 성공했습니다."),

    // 카카오 로그인 성공 코드
    KAKAO_LOGIN_200_1(HttpStatus.OK, "KAKAO_LOGIN_200_1", "처음으로 카카오 로그인에 성공했습니다."),
    KAKAO_LOGIN_200_2(HttpStatus.OK, "KAKAO_LOGIN_200_2", "카카오 로그인에 성공했습니다."),

    // 로컬 회원가입 / 로그인 성공 코드
    LOCAL_SIGNUP_200_1(HttpStatus.OK, "LOCAL_SIGNUP_200_1", "회원가입에 성공했습니다."),
    LOCAL_LOGIN_200_1(HttpStatus.OK, "LOCAL_LOGIN_200_1", "처음으로 로컬 로그인에 성공했습니다."),
    LOCAL_LOGIN_200_2(HttpStatus.OK, "LOCAL_LOGIN_200_2", "로컬 로그인에 성공했습니다."),
    NICKNAME_200(HttpStatus.OK, "NICKNAME_200", "사용 가능한 닉네임입니다."),
    USERNAME_200(HttpStatus.OK, "USERNAME_200", "사용 가능한 아이디 입니다."),
    TERM_200(HttpStatus.OK, "TERM_200", "약관 동의에 성공했습니다."),

    // 이메일 인증
    EMAIL_SEND_200(HttpStatus.OK, "EMAIL_SEND_200", "인증번호가 전송되었습니다."),
    EMAIL_VERIFY_200(HttpStatus.OK, "EMAIL_VERIFY_200", "이메일 인증에 성공했습니다."),

    // 전화번호 인증
    PHONE_SEND_200(HttpStatus.OK, "PHONE_SEND_200", "인증번호가 전송되었습니다."),
    PHONE_200(HttpStatus.OK, "PHONE_200", "전화번호 인증 성공"),
    PHONE_201(HttpStatus.OK, "PHONE_201", "로그인 연동 단계로 넘어갑니다."),

    // 로그인 연동
    LOGIN_LINK_200(HttpStatus.OK, "LOGIN_LINK_200", "로그인 연동 완료"),
    LOGIN_LINK_201(HttpStatus.OK, "LOGIN_LINK_201", "기존 계정과 연동이 필요합니다. 기존 로그인 수단으로 확인해주세요."),
    ONBOARDING_EMAIL_200(HttpStatus.OK, "ONBOARDING_EMAIL_200", "이메일이 등록되었습니다."),
    ONBOARDING_200(HttpStatus.OK, "ONBOARDING_200", "온보딩 완료"),
    PROFILE_IMAGE_200(HttpStatus.OK, "PROFILE_IMAGE_200", "유저 프로필 이미지 등록에 성공했습니다."),

    REFRESH_200(HttpStatus.OK, "REFRESH_200", "토큰 재발급에 성공했습니다."),
    LOGOUT_200(HttpStatus.OK, "LOGOUT_200", "로그아웃 되었습니다."),
    SIGNOUT_200(HttpStatus.OK, "SIGNOUT_200", "회원탈퇴 되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

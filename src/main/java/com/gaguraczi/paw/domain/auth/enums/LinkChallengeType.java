package com.gaguraczi.paw.domain.auth.enums;

public enum LinkChallengeType {
    /** 로컬 가입/로그인 시도 → 카카오로 기존 계정 확인 후 LOCAL 연동 */
    NEED_KAKAO_CONFIRM,
    /** 카카오 로그인 시 동일 이메일 로컬 계정 → 로컬 비밀번호 확인 후 KAKAO 연동 */
    NEED_LOCAL_CONFIRM,
    /** 온보딩 이메일 입력 → 로컬 비밀번호 확인 후 카카오 User를 로컬 User로 병합 */
    NEED_LOCAL_CONFIRM_MERGE
}

package com.gaguraczi.paw.domain.auth.dto.res;

import lombok.Builder;
import lombok.Getter;

/**
 * 연동 확인 창을 띄우기 위한 챌린지 응답.
 * existingProvider로 기존 로그인 수단을 한 번 확인한 뒤 /auth/link/confirm/* 를 호출한다.
 */
@Getter
@Builder
public class LoginLinkChallengeRes {

    private final String linkToken;
    /** 확인에 사용할 기존 로그인 수단: LOCAL 또는 KAKAO */
    private final String existingProvider;
    private final String email;
}

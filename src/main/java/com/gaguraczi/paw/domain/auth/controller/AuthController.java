package com.gaguraczi.paw.domain.auth.controller;

import com.gaguraczi.paw.domain.auth.dto.req.EmailSendReq;
import com.gaguraczi.paw.domain.auth.dto.req.EmailVerifyReq;
import com.gaguraczi.paw.domain.auth.dto.req.KakaoLoginReq;
import com.gaguraczi.paw.domain.auth.dto.req.LinkConfirmKakaoReq;
import com.gaguraczi.paw.domain.auth.dto.req.LinkConfirmLocalReq;
import com.gaguraczi.paw.domain.auth.dto.req.LocalLoginReq;
import com.gaguraczi.paw.domain.auth.dto.req.LocalSignupReq;
import com.gaguraczi.paw.domain.auth.dto.req.OnboardingProfileReq;
import com.gaguraczi.paw.domain.auth.dto.req.RefreshTokenReq;
import com.gaguraczi.paw.domain.auth.exception.code.AuthSuccessCode;
import com.gaguraczi.paw.domain.auth.service.AuthService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로컬 회원가입")
    @PostMapping("/signup/local")
    public ApiResponse<?> signupLocal(@Valid @RequestBody LocalSignupReq req) {
        AuthService.AuthResult result = authService.signupLocal(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "이메일 인증번호 전송")
    @PostMapping("/email/send")
    public ApiResponse<?> sendEmailCode(@Valid @RequestBody EmailSendReq req) {
        AuthService.AuthResult result = authService.sendEmailCode(req.getEmail());
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "이메일 인증번호 확인")
    @PostMapping("/email/verify")
    public ApiResponse<?> verifyEmailCode(@Valid @RequestBody EmailVerifyReq req) {
        AuthService.AuthResult result = authService.verifyEmailCode(req.getEmail(), req.getCode());
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "로컬 로그인")
    @PostMapping("/login/local")
    public ApiResponse<?> loginLocal(@Valid @RequestBody LocalLoginReq req) {
        AuthService.AuthResult result = authService.loginLocal(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "카카오 로그인")
    @PostMapping("/login/kakao")
    public ApiResponse<?> loginKakao(@Valid @RequestBody KakaoLoginReq req) {
        AuthService.AuthResult result = authService.loginKakao(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "카카오 온보딩 - 이름, 닉네임, 한줄소개")
    @PostMapping("/onboarding")
    public ApiResponse<?> completeKakaoOnboarding(@Valid @RequestBody OnboardingProfileReq req) {
        AuthService.AuthResult result = authService.completeKakaoOnboarding(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "로그인 상태에서 카카오 계정 연동")
    @PostMapping("/link/kakao")
    public ApiResponse<?> linkKakao(@Valid @RequestBody KakaoLoginReq req) {
        AuthService.AuthResult result = authService.linkKakao(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "연동 확인 - 카카오(기존 소셜 계정 확인 후 로컬 연동)")
    @PostMapping("/link/confirm/kakao")
    public ApiResponse<?> confirmLinkKakao(@Valid @RequestBody LinkConfirmKakaoReq req) {
        AuthService.AuthResult result = authService.confirmLinkWithKakao(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "연동 확인 - 로컬(온보딩 시 로컬 비밀번호 확인 후 카카오 병합)")
    @PostMapping("/link/confirm/local")
    public ApiResponse<?> confirmLinkLocal(@Valid @RequestBody LinkConfirmLocalReq req) {
        AuthService.AuthResult result = authService.confirmLinkWithLocal(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ApiResponse<?> reissue(@Valid @RequestBody RefreshTokenReq req) {
        AuthService.AuthResult result = authService.reissue(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenReq req) {
        authService.logout(req);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_200, null);
    }
}

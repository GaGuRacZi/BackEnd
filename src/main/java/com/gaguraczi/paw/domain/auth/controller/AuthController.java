package com.gaguraczi.paw.domain.auth.controller;

import com.gaguraczi.paw.domain.auth.dto.req.EmailSendReq;
import com.gaguraczi.paw.domain.auth.dto.req.EmailVerifyReq;
import com.gaguraczi.paw.domain.auth.dto.req.KakaoLoginReq;
import com.gaguraczi.paw.domain.auth.dto.req.LinkConfirmKakaoReq;
import com.gaguraczi.paw.domain.auth.dto.req.LinkConfirmLocalReq;
import com.gaguraczi.paw.domain.auth.dto.req.LocalLoginReq;
import com.gaguraczi.paw.domain.auth.dto.req.LocalSignupReq;
import com.gaguraczi.paw.domain.auth.dto.req.OnboardingCompleteReq;
import com.gaguraczi.paw.domain.auth.dto.req.RefreshTokenReq;
import com.gaguraczi.paw.domain.auth.exception.code.AuthSuccessCode;
import com.gaguraczi.paw.domain.auth.service.AuthService;
import com.gaguraczi.paw.domain.users.dto.res.UserProfileRes;
import com.gaguraczi.paw.domain.users.service.UserService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

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

    @Operation(
            summary = "온보딩 완료 - 보호자/좌표/약관 등록 (시군구는 좌표로 자동 확정, 펫은 POST /pets)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "필수약관만 동의",
                                            value = """
                                                    {
                                                      "name": "홍길동",
                                                      "nickname": "길동이",
                                                      "intro": "강아지와 산책하는 걸 좋아해요",
                                                      "location": {
                                                        "latitude": 37.5665,
                                                        "longitude": 126.9780
                                                      },
                                                      "agreements": {
                                                        "AGE_OVER_14": true,
                                                        "TERMS_OF_SERVICE": true,
                                                        "PRIVACY": true,
                                                        "PROFILE_EXTRA": true,
                                                        "MARKETING_PUSH": false,
                                                        "LOCATION_SERVICE": false
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "전체 약관 동의",
                                            value = """
                                                    {
                                                      "name": "김나비",
                                                      "nickname": "냥집사",
                                                      "intro": "고양이 집사입니다",
                                                      "location": {
                                                        "latitude": 35.1796,
                                                        "longitude": 129.0756
                                                      },
                                                      "agreements": {
                                                        "AGE_OVER_14": true,
                                                        "TERMS_OF_SERVICE": true,
                                                        "PRIVACY": true,
                                                        "PROFILE_EXTRA": true,
                                                        "MARKETING_PUSH": true,
                                                        "LOCATION_SERVICE": true
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @PostMapping("/onboarding")
    public ApiResponse<?> completeOnboarding(@Valid @RequestBody OnboardingCompleteReq req) {
        AuthService.AuthResult result = authService.completeOnboarding(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "유저 프로필 이미지 등록",
            description = "multipart/form-data: image(파일, 필수). 온보딩과 분리된 프로필 이미지 전용 API입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ProfileImageMultipart.class),
                            encoding = {
                                    @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileRes> uploadProfileImage(
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                AuthSuccessCode.PROFILE_IMAGE_200,
                userService.updateMyProfileImage(image)
        );
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

    @Schema(name = "ProfileImageMultipart", description = "유저 프로필 이미지 등록 multipart")
    public static class ProfileImageMultipart {
        @Schema(description = "프로필 이미지", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED)
        public MultipartFile image;
    }
}

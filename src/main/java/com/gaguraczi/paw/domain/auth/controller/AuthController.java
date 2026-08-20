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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "로컬 회원가입",
            description = """
                    이메일 인증(`POST /auth/email/verify`) 완료 후 호출합니다. JWT 불필요(permitAll).
                    - 성공 시 LoginRes(access/refresh) 또는 기존 카카오 계정과 충돌 시 LOGIN_LINK_201 챌린지
                    - 이메일 미인증: EMAIL_NOT_VERIFIED / 이미 가입: LOCAL_SIGNUP_409_1
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로컬 회원가입",
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "password": "password123!"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공 또는 연동 챌린지",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "회원가입 성공",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LOCAL_SIGNUP_200_1",
                                                      "message": "회원가입에 성공했습니다.",
                                                      "result": {
                                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.sample",
                                                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample",
                                                        "isNew": true,
                                                        "uid": "550e8400-e29b-41d4-a716-446655440000"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "연동 챌린지",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LOGIN_LINK_201",
                                                      "message": "기존 계정과 연동이 필요합니다. 기존 로그인 수단으로 확인해주세요.",
                                                      "result": {
                                                        "linkToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                        "existingProvider": "KAKAO",
                                                        "email": "user@example.com"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류 / 이메일 미인증",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "COMMON_400",
                                            value = """
                                                    {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "EMAIL_NOT_VERIFIED",
                                            value = """
                                                    {"isSuccess":false,"code":"EMAIL_NOT_VERIFIED","message":"이메일 인증이 필요합니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 계정",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOCAL_SIGNUP_409_1",
                                    value = """
                                            {"isSuccess":false,"code":"LOCAL_SIGNUP_409_1","message":"이미 존재하는 아이디입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/signup/local")
    public ApiResponse<?> signupLocal(@Valid @RequestBody LocalSignupReq req) {
        AuthService.AuthResult result = authService.signupLocal(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "이메일 인증번호 전송",
            description = """
                    로컬 가입/카카오 계정 연동 진입용 6자리 인증번호를 메일로 전송합니다. JWT 불필요(permitAll).
                    Redis TTL 5분, 재전송 쿨다운 60초(EMAIL_SEND_429).
                    KAKAO만 있는 이메일은 연동 챌린지(LOGIN_LINK_201) 진입을 위해 발송을 허용합니다.
                    LOCAL이 이미 있거나 연동 불가 계정이면 LOCAL_SIGNUP_409_1.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "이메일 전송",
                                    value = """
                                            { "email": "user@example.com" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "전송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "EMAIL_SEND_200",
                                    value = """
                                            {"isSuccess":true,"code":"EMAIL_SEND_200","message":"인증번호가 전송되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "전송 실패 / 유효성 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "COMMON_400",
                                            value = """
                                                    {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "EMAIL_SEND_400",
                                            value = """
                                                    {"isSuccess":false,"code":"EMAIL_SEND_400","message":"인증번호 전송에 실패했습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "재전송 쿨다운",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "EMAIL_SEND_429",
                                    value = """
                                            {"isSuccess":false,"code":"EMAIL_SEND_429","message":"인증번호 재전송은 잠시 후 다시 시도해 주세요.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 로컬 가입되었거나 연동 불가 계정",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOCAL_SIGNUP_409_1",
                                    value = """
                                            {"isSuccess":false,"code":"LOCAL_SIGNUP_409_1","message":"이미 존재하는 아이디입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/email/send")
    public ApiResponse<?> sendEmailCode(@Valid @RequestBody EmailSendReq req) {
        AuthService.AuthResult result = authService.sendEmailCode(req.getEmail());
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "이메일 인증번호 확인",
            description = "전송된 6자리 코드를 검증합니다. 성공 시 가입 가능 플래그가 Redis에 저장됩니다(TTL 30분). JWT 불필요.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 확인",
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "code": "123456"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "인증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "EMAIL_VERIFY_200",
                                    value = """
                                            {"isSuccess":true,"code":"EMAIL_VERIFY_200","message":"이메일 인증에 성공했습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "인증번호 불일치/만료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "COMMON_400",
                                            value = """
                                                    {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "EMAIL_CODE_400",
                                            value = """
                                                    {"isSuccess":false,"code":"EMAIL_CODE_400","message":"인증번호가 일치하지 않거나 만료되었습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/email/verify")
    public ApiResponse<?> verifyEmailCode(@Valid @RequestBody EmailVerifyReq req) {
        AuthService.AuthResult result = authService.verifyEmailCode(req.getEmail(), req.getCode());
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "로컬 로그인",
            description = """
                    이메일/비밀번호로 로그인합니다. JWT 불필요(permitAll).
                    - 신규 시 LoginRes / 카카오만 있는 이메일이면 LOGIN_LINK_201
                    - 실패: LOCAL_LOGIN_401_2
                    - 탈퇴 계정도 LOCAL_LOGIN_401_2 (비밀번호 오류와 동일 코드, 재가입 안 됨)
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로컬 로그인",
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "password": "password123!"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 또는 연동 챌린지",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "로그인 성공",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LOCAL_LOGIN_200_2",
                                                      "message": "로컬 로그인에 성공했습니다.",
                                                      "result": {
                                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.sample",
                                                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample",
                                                        "isNew": false,
                                                        "uid": "550e8400-e29b-41d4-a716-446655440000"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "첫 로그인",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LOCAL_LOGIN_200_1",
                                                      "message": "처음으로 로컬 로그인에 성공했습니다.",
                                                      "result": {
                                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.sample",
                                                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample",
                                                        "isNew": true,
                                                        "uid": "550e8400-e29b-41d4-a716-446655440000"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "연동 챌린지",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LOGIN_LINK_201",
                                                      "message": "기존 계정과 연동이 필요합니다. 기존 로그인 수단으로 확인해주세요.",
                                                      "result": {
                                                        "linkToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                        "existingProvider": "KAKAO",
                                                        "email": "user@example.com"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMON_400",
                                    value = """
                                            {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "아이디/비밀번호 불일치 또는 탈퇴 계정 (LOCAL_LOGIN_401_2)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOCAL_LOGIN_401_2",
                                    value = """
                                            {"isSuccess":false,"code":"LOCAL_LOGIN_401_2","message":"아이디 또는 비밀번호가 올바르지 않습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login/local")
    public ApiResponse<?> loginLocal(@Valid @RequestBody LocalLoginReq req) {
        AuthService.AuthResult result = authService.loginLocal(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "카카오 로그인",
            description = """
                    카카오 accessToken으로 로그인/가입합니다. JWT 불필요(permitAll).
                    - 성공: KAKAO_LOGIN_200_1(신규) / KAKAO_LOGIN_200_2(기존)
                    - 동일 이메일의 로컬 계정이 있으면 LOGIN_LINK_201(existingProvider=LOCAL)
                    - 탈퇴 계정은 LOCAL_LOGIN_401_2 (재가입 안 됨)
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "카카오 로그인",
                                    value = """
                                            { "accessToken": "kakao_access_token_sample" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 또는 연동 챌린지",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "카카오 로그인 성공",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "KAKAO_LOGIN_200_2",
                                                      "message": "카카오 로그인에 성공했습니다.",
                                                      "result": {
                                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.sample",
                                                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample",
                                                        "isNew": false,
                                                        "uid": "550e8400-e29b-41d4-a716-446655440000"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "연동 챌린지",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "LOGIN_LINK_201",
                                                      "message": "기존 계정과 연동이 필요합니다. 기존 로그인 수단으로 확인해주세요.",
                                                      "result": {
                                                        "linkToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                        "existingProvider": "LOCAL",
                                                        "email": "user@example.com"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성/연동 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "COMMON_400",
                                            value = """
                                                    {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "LOGIN_LINK_400_3",
                                            value = """
                                                    {"isSuccess":false,"code":"LOGIN_LINK_400_3","message":"해당 소셜 타입은 이미 연동되어 있습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "카카오 토큰 무효 또는 탈퇴 계정",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "KAKAO_LOGIN_401",
                                            value = """
                                                    {"isSuccess":false,"code":"KAKAO_LOGIN_401","message":"유효하지 않은 카카오 토큰입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "LOCAL_LOGIN_401_2",
                                            summary = "탈퇴 계정",
                                            value = """
                                                    {"isSuccess":false,"code":"LOCAL_LOGIN_401_2","message":"아이디 또는 비밀번호가 올바르지 않습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 계정",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOCAL_SIGNUP_409_1",
                                    value = """
                                            {"isSuccess":false,"code":"LOCAL_SIGNUP_409_1","message":"이미 존재하는 아이디입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login/kakao")
    public ApiResponse<?> loginKakao(@Valid @RequestBody KakaoLoginReq req) {
        AuthService.AuthResult result = authService.loginKakao(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "온보딩 완료 - 보호자/좌표/약관 등록 (시군구는 좌표로 자동 확정, 펫은 POST /pets)",
            description = """
                    Access Token(JWT) 필수. isNew 유저의 이름·닉네임·한줄소개·위치·약관을 등록하고 isNew=false로 전환합니다.
                    필수 약관(AGE_OVER_14, TERMS_OF_SERVICE, PRIVACY, PROFILE_EXTRA)은 true여야 합니다.
                    """,
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "온보딩 완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "ONBOARDING_200",
                                    value = """
                                            {"isSuccess":true,"code":"ONBOARDING_200","message":"온보딩 완료","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "온보딩/약관/닉네임 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "ONBOARDING_400",
                                            value = """
                                                    {"isSuccess":false,"code":"ONBOARDING_400","message":"온보딩 실패","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "TERM_400",
                                            value = """
                                                    {"isSuccess":false,"code":"TERM_400","message":"약관 동의가 올바르지 않습니다. 허용 타입: AGE_OVER_14, TERMS_OF_SERVICE, PRIVACY, PROFILE_EXTRA, MARKETING_PUSH, LOCATION_SERVICE","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "NICKNAME_400",
                                            value = """
                                                    {"isSuccess":false,"code":"NICKNAME_400","message":"닉네임은 15자 이내의 영문, 숫자, 한글만 사용 가능합니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "유효하지 않은 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "닉네임 중복",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "NICKNAME_409",
                                    value = """
                                            {"isSuccess":false,"code":"NICKNAME_409","message":"이미 사용 중인 닉네임입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/onboarding")
    public ApiResponse<?> completeOnboarding(@Valid @RequestBody OnboardingCompleteReq req) {
        AuthService.AuthResult result = authService.completeOnboarding(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "유저 프로필 이미지 등록",
            description = "multipart/form-data: image(파일, 필수). JWT 필수. 온보딩과 분리된 프로필 이미지 전용 API입니다.",
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 이미지 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PROFILE_IMAGE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PROFILE_IMAGE_200",
                                              "message": "유저 프로필 이미지 등록에 성공했습니다.",
                                              "result": {
                                                "uid": "550e8400-e29b-41d4-a716-446655440000",
                                                "name": "홍길동",
                                                "nickname": "길동이",
                                                "intro": "강아지와 산책하는 걸 좋아해요",
                                                "email": "user@example.com",
                                                "profileUrl": "https://cdn.example.com/profiles/uid.jpg",
                                                "regionCode": "11680",
                                                "regionName": "강남구",
                                                "isNew": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "이미지 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "USER_PROFILE_400_1",
                                            value = """
                                                    {"isSuccess":false,"code":"USER_PROFILE_400_1","message":"비어 있는 이미지 파일은 업로드할 수 없습니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "USER_PROFILE_400_2",
                                            value = """
                                                    {"isSuccess":false,"code":"USER_PROFILE_400_2","message":"프로필 이미지는 5MB 이하여야 합니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "USER_PROFILE_400_3",
                                            value = """
                                                    {"isSuccess":false,"code":"USER_PROFILE_400_3","message":"지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileRes> uploadProfileImage(
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                AuthSuccessCode.PROFILE_IMAGE_200,
                userService.updateMyProfileImage(image)
        );
    }

    @Operation(
            summary = "로그인 상태에서 카카오 계정 연동",
            description = "Access Token(JWT) 필수. 현재 로컬 계정에 카카오를 추가 연동합니다. 이미 연동된 경우 LOGIN_LINK_400_3.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "카카오 연동",
                                    value = """
                                            { "accessToken": "kakao_access_token_sample" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "연동 완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOGIN_LINK_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "LOGIN_LINK_200",
                                              "message": "로그인 연동 완료",
                                              "result": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.sample",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample",
                                                "isNew": false,
                                                "uid": "550e8400-e29b-41d4-a716-446655440000"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "연동 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "LOGIN_LINK_400",
                                            value = """
                                                    {"isSuccess":false,"code":"LOGIN_LINK_400","message":"로그인 연동 실패","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "LOGIN_LINK_400_3",
                                            value = """
                                                    {"isSuccess":false,"code":"LOGIN_LINK_400_3","message":"해당 소셜 타입은 이미 연동되어 있습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 또는 카카오 토큰 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "JWT_401_1",
                                            value = """
                                                    {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "KAKAO_LOGIN_401",
                                            value = """
                                                    {"isSuccess":false,"code":"KAKAO_LOGIN_401","message":"유효하지 않은 카카오 토큰입니다.","result":null}
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/link/kakao")
    public ApiResponse<?> linkKakao(@Valid @RequestBody KakaoLoginReq req) {
        AuthService.AuthResult result = authService.linkKakao(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "연동 확인 - 카카오(기존 소셜 계정 확인 후 로컬 연동)",
            description = """
                    JWT 불필요(permitAll). LOGIN_LINK_201에서 existingProvider=KAKAO일 때 호출.
                    linkToken + 카카오 accessToken으로 기존 계정을 확인한 뒤 로컬을 연동합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "카카오 확인",
                                    value = """
                                            {
                                              "linkToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                              "accessToken": "kakao_access_token_sample"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "연동 완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOGIN_LINK_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "LOGIN_LINK_200",
                                              "message": "로그인 연동 완료",
                                              "result": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.sample",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample",
                                                "isNew": false,
                                                "uid": "550e8400-e29b-41d4-a716-446655440000"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "linkToken 무효 / 연동 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOGIN_LINK_400",
                                    value = """
                                            {"isSuccess":false,"code":"LOGIN_LINK_400","message":"로그인 연동 실패","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "카카오 토큰 무효",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "KAKAO_LOGIN_401",
                                    value = """
                                            {"isSuccess":false,"code":"KAKAO_LOGIN_401","message":"유효하지 않은 카카오 토큰입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/link/confirm/kakao")
    public ApiResponse<?> confirmLinkKakao(@Valid @RequestBody LinkConfirmKakaoReq req) {
        AuthService.AuthResult result = authService.confirmLinkWithKakao(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "연동 확인 - 로컬(온보딩 시 로컬 비밀번호 확인 후 카카오 병합)",
            description = """
                    JWT 불필요(permitAll). LOGIN_LINK_201에서 existingProvider=LOCAL일 때 호출.
                    linkToken + 로컬 비밀번호로 기존 계정을 확인한 뒤 카카오를 병합합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로컬 확인",
                                    value = """
                                            {
                                              "linkToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                              "password": "password123!"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "연동 완료",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOGIN_LINK_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "LOGIN_LINK_200",
                                              "message": "로그인 연동 완료",
                                              "result": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.sample",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample",
                                                "isNew": false,
                                                "uid": "550e8400-e29b-41d4-a716-446655440000"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "linkToken 무효 / 연동 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOGIN_LINK_400",
                                    value = """
                                            {"isSuccess":false,"code":"LOGIN_LINK_400","message":"로그인 연동 실패","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "비밀번호 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOCAL_LOGIN_401_2",
                                    value = """
                                            {"isSuccess":false,"code":"LOCAL_LOGIN_401_2","message":"아이디 또는 비밀번호가 올바르지 않습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/link/confirm/local")
    public ApiResponse<?> confirmLinkLocal(@Valid @RequestBody LinkConfirmLocalReq req) {
        AuthService.AuthResult result = authService.confirmLinkWithLocal(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token으로 Access/Refresh를 재발급합니다. JWT 헤더 불필요(permitAll). 실패 시 REFRESH_401.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "재발급",
                                    value = """
                                            { "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "REFRESH_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REFRESH_200",
                                              "message": "토큰 재발급에 성공했습니다.",
                                              "result": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access.new",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.new",
                                                "isNew": false,
                                                "uid": "550e8400-e29b-41d4-a716-446655440000"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMON_400",
                                    value = """
                                            {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "refresh 무효",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "REFRESH_401",
                                    value = """
                                            {"isSuccess":false,"code":"REFRESH_401","message":"토큰 재발급에 실패했습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/reissue")
    public ApiResponse<?> reissue(@Valid @RequestBody RefreshTokenReq req) {
        AuthService.AuthResult result = authService.reissue(req);
        return ApiResponse.onSuccess(result.successCode(), result.result());
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 Redis에서 삭제합니다. JWT 헤더 불필요(permitAll). 실패 시 LOGOUT_401.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로그아웃",
                                    value = """
                                            { "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOGOUT_200",
                                    value = """
                                            {"isSuccess":true,"code":"LOGOUT_200","message":"로그아웃 되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMON_400",
                                    value = """
                                            {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 refresh",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "LOGOUT_401",
                                    value = """
                                            {"isSuccess":false,"code":"LOGOUT_401","message":"유효하지 않은 토큰입니다.","result":null}
                                            """
                            )
                    )
            )
    })
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

package com.gaguraczi.paw.domain.users.controller;

import com.gaguraczi.paw.domain.users.dto.req.PushTokenUpdateReq;
import com.gaguraczi.paw.domain.users.dto.req.UserProfileUpdateReq;
import com.gaguraczi.paw.domain.users.dto.res.UserProfileRes;
import com.gaguraczi.paw.domain.users.exception.code.UserSuccessCode;
import com.gaguraczi.paw.domain.users.service.UserService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "users", description = "유저 프로필 API. JWT Bearer 필수. FCM 토큰은 PUT /users/me/push-token.")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 프로필 조회",
            description = """
                    Access Token(JWT) 필수. 현재 로그인 유저의 프로필(이름, 닉네임, 지역, 코인, isNew 등)을 반환합니다.
                    `coin`은 남은 코인, `usedCoin`은 사용한 누적입니다. `POST /visits/{visitId}/ai-summary`가 코인 1개를 쓰며, 생성 실패 시 환불됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "USER_PROFILE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "USER_PROFILE_200",
                                              "message": "프로필 조회에 성공했습니다.",
                                              "result": {
                                                "uid": "550e8400-e29b-41d4-a716-446655440000",
                                                "name": "홍길동",
                                                "nickname": "길동이",
                                                "intro": "강아지와 산책하는 걸 좋아해요",
                                                "email": "user@example.com",
                                                "profileUrl": "https://cdn.example.com/profiles/uid.jpg",
                                                "regionCode": "11680",
                                                "regionName": "강남구",
                                                "isNew": false,
                                                "coin": 3,
                                                "usedCoin": 1
                                              }
                                            }
                                            """
                            )
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
                    responseCode = "404",
                    description = "회원 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "USER_404_1",
                                    value = """
                                            {"isSuccess":false,"code":"USER_404_1","message":"존재하지 않는 회원입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/me")
    public ApiResponse<UserProfileRes> getMyProfile() {
        return ApiResponse.onSuccess(UserSuccessCode.USER_PROFILE_200, userService.getMyProfile());
    }

    @Operation(
            summary = "내 프로필 수정 (이미지 포함 가능)",
            description = """
                    Access Token(JWT) 필수. multipart/form-data: data(JSON, 선택) + image(파일, 선택).
                    data만, image만, 둘 다 전송 가능. 닉네임 규칙: 15자 이내 영문/숫자/한글.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UserProfileMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = @ExampleObject(
                                    name = "data JSON 예시",
                                    value = """
                                            {
                                              "name": "홍길동",
                                              "nickname": "길동이",
                                              "intro": "강아지와 산책하는 걸 좋아해요"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "USER_PROFILE_UPDATE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "USER_PROFILE_UPDATE_200",
                                              "message": "프로필이 수정되었습니다.",
                                              "result": {
                                                "uid": "550e8400-e29b-41d4-a716-446655440000",
                                                "name": "홍길동",
                                                "nickname": "길동이",
                                                "intro": "강아지와 산책하는 걸 좋아해요",
                                                "email": "user@example.com",
                                                "profileUrl": "https://cdn.example.com/profiles/uid.jpg",
                                                "regionCode": "11680",
                                                "regionName": "강남구",
                                                "isNew": false,
                                                "coin": 3,
                                                "usedCoin": 1
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성/이미지 오류",
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
                                            name = "USER_PROFILE_400",
                                            value = """
                                                    {"isSuccess":false,"code":"USER_PROFILE_400","message":"프로필 수정에 실패했습니다.","result":null}
                                                    """
                                    ),
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
            )
    })
    @PutMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileRes> updateMyProfile(
            @RequestPart(value = "data", required = false) @Valid UserProfileUpdateReq data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                UserSuccessCode.USER_PROFILE_UPDATE_200,
                userService.updateMyProfile(data, image)
        );
    }

    @Operation(
            summary = "FCM 푸시 토큰 등록/해제",
            description = """
                    Access Token(JWT) 필수.
                    - 로그인 직후 디바이스 FCM 토큰을 올립니다. 채널 on/off는 `PATCH /mypage/notifications/settings`
                    - 채팅 푸시도 이 토큰으로 갑니다. `chatAlarm`이 꺼져 있으면 토큰이 있어도 채팅 FCM은 나가지 않습니다.
                    - `pushToken`이 null이거나 공백이면 서버에서 토큰을 지웁니다 (로그아웃 시 호출 권장)
                    - 회원 탈퇴 시에도 토큰은 서버에서 비워집니다
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PushTokenUpdateReq.class),
                            examples = {
                                    @ExampleObject(
                                            name = "등록",
                                            value = """
                                                    { "pushToken": "fcm-device-token" }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "해제",
                                            value = """
                                                    { "pushToken": "" }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (USER_PUSH_TOKEN_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "변경 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "USER_PUSH_TOKEN_200",
                                              "message": "푸시 토큰이 변경되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
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
    @PutMapping("/me/push-token")
    public ApiResponse<Void> updatePushToken(
            @org.springframework.web.bind.annotation.RequestBody PushTokenUpdateReq req
    ) {
        userService.updatePushToken(req == null ? null : req.pushToken());
        return ApiResponse.onSuccess(UserSuccessCode.USER_PUSH_TOKEN_200, null);
    }

    @Schema(name = "UserProfileMultipart", description = "유저 프로필 수정 multipart")
    public static class UserProfileMultipart {
        @Schema(description = "프로필 정보 JSON", implementation = UserProfileUpdateReq.class)
        public UserProfileUpdateReq data;

        @Schema(description = "프로필 이미지", type = "string", format = "binary")
        public MultipartFile image;
    }
}

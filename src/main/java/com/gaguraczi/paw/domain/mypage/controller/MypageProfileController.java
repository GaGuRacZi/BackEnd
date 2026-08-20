package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.req.MypageRegionUpdateReq;
import com.gaguraczi.paw.domain.mypage.dto.res.MypageHomeRes;
import com.gaguraczi.paw.domain.mypage.dto.res.MypageProfileRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.MypageProfileService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "mypage",
        description = "마이페이지 API. 공지사항 목록/상세만 인증 불필요. 그 외 JWT Bearer 필수. "
                + "커서(nextCursor)는 opaque 값으로 다음 요청에 그대로 전달하세요."
)
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageProfileController {

    private final MypageProfileService mypageProfileService;

    @Operation(
            summary = "마이페이지 홈 요약 조회",
            description = """
                    Access Token(JWT) 필수.
                    - 닉네임, 표시용 지역명, 대표 반려동물, 구독 요약, 미읽음 알림 수를 반환합니다.
                    - 지역명은 가능하면 `시/도 시군구` (예: 서울특별시 강남구)입니다.
                    - 대표 펫이 없으면 `mainPet`은 null입니다.
                    - 프로필 수정은 `PUT /users/me/profile`, 펫 전환은 `PATCH /pets/{petId}/main`을 사용하세요.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_HOME_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "홈 요약",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_HOME_200",
                                              "message": "마이페이지 홈 조회에 성공했습니다.",
                                              "result": {
                                                "name": "홍길동",
                                                "nickname": "길동이",
                                                "profileUrl": "https://cdn.example.com/profiles/uid.jpg",
                                                "regionName": "서울특별시 강남구",
                                                "mainPet": {
                                                  "petId": 1,
                                                  "petName": "초코",
                                                  "profileUrl": "https://cdn.example.com/pets/1.jpg"
                                                },
                                                "subscribe": {
                                                  "plan": "BASIC",
                                                  "displayName": "꼬마 젤리",
                                                  "active": true
                                                },
                                                "unreadNotificationCount": 3
                                              }
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
    @GetMapping("/home")
    public ApiResponse<MypageHomeRes> getHome() {
        return ApiResponse.onSuccess(MypageSuccessCode.MYPAGE_HOME_200, mypageProfileService.getHome());
    }

    @Operation(
            summary = "프로필 상세 조회",
            description = """
                    Access Token(JWT) 필수.
                    - 기본 프로필 + 연동된 로그인 수단(`linkedAccounts`)을 반환합니다.
                    - 코인/usedCoin은 이 API에 없고 `GET /users/me`를 사용하세요.
                    - 프로필 사진 삭제는 `DELETE /mypage/profile/image`입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_PROFILE_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "프로필 상세",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_PROFILE_200",
                                              "message": "프로필 상세 조회에 성공했습니다.",
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
                                                "linkedAccounts": [
                                                  { "socialType": "KAKAO", "linkedAt": "2026-03-01T12:00:00" },
                                                  { "socialType": "LOCAL", "linkedAt": "2026-03-02T09:30:00" }
                                                ]
                                              }
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
    @GetMapping("/profile")
    public ApiResponse<MypageProfileRes> getProfile() {
        return ApiResponse.onSuccess(MypageSuccessCode.MYPAGE_PROFILE_200, mypageProfileService.getProfile());
    }

    @Operation(
            summary = "프로필 사진 삭제",
            description = """
                    Access Token(JWT) 필수.
                    - S3 키/URL을 비우고 기존 파일은 커밋 후 삭제합니다.
                    - 등록된 사진이 없어도 200 (idempotent).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_PROFILE_IMAGE_DELETE_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "삭제 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_PROFILE_IMAGE_DELETE_200",
                                              "message": "프로필 사진이 삭제되었습니다.",
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
    @DeleteMapping("/profile/image")
    public ApiResponse<Void> deleteProfileImage() {
        mypageProfileService.deleteProfileImage();
        return ApiResponse.onSuccess(MypageSuccessCode.MYPAGE_PROFILE_IMAGE_DELETE_200, null);
    }

    @Operation(
            summary = "지역 설정 (주소 검색 결과 선택 저장)",
            description = """
                    Access Token(JWT) 필수.
                    - `GET /regions/search`로 고른 `regionCode`를 저장합니다.
                    - 현재 위치 기반 자동 설정은 `POST /location/user/cert`를 사용하세요.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "지역 코드 저장",
                                    value = """
                                            { "regionCode": "11680" }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_REGION_UPDATE_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "설정 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_REGION_UPDATE_200",
                                              "message": "지역이 설정되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "regionCode 누락 (COMMON_400)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMON_400",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON_400",
                                              "message": "잘못된 요청입니다.",
                                              "result": { "regionCode": "regionCode는 필수입니다." }
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 지역 코드 (MYPAGE_404_2)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MYPAGE_404_2",
                                    value = """
                                            {"isSuccess":false,"code":"MYPAGE_404_2","message":"존재하지 않는 지역 코드입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/region")
    public ApiResponse<Void> updateRegion(@Valid @RequestBody MypageRegionUpdateReq req) {
        mypageProfileService.updateRegion(req.regionCode());
        return ApiResponse.onSuccess(MypageSuccessCode.MYPAGE_REGION_UPDATE_200, null);
    }
}

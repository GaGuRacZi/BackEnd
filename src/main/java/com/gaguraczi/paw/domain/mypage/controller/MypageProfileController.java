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

@Tag(name = "mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageProfileController {

    private final MypageProfileService mypageProfileService;

    @Operation(
            summary = "마이페이지 홈 요약 조회",
            description = "Access Token(JWT) 필수. 닉네임, 지역, 대표 반려동물, 구독 요약 정보를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = "{\"isSuccess\":false,\"code\":\"JWT_401_1\",\"message\":\"token 유효기간이 만료되었습니다.\",\"result\":null}"
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
            description = "Access Token(JWT) 필수. 기본 프로필 정보에 연동된 계정 목록이 포함됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/profile")
    public ApiResponse<MypageProfileRes> getProfile() {
        return ApiResponse.onSuccess(MypageSuccessCode.MYPAGE_PROFILE_200, mypageProfileService.getProfile());
    }

    @Operation(
            summary = "프로필 사진 삭제",
            description = "Access Token(JWT) 필수. 등록된 사진이 없어도 idempotent하게 200을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/profile/image")
    public ApiResponse<Void> deleteProfileImage() {
        mypageProfileService.deleteProfileImage();
        return ApiResponse.onSuccess(MypageSuccessCode.MYPAGE_PROFILE_IMAGE_DELETE_200, null);
    }

    @Operation(
            summary = "지역 설정 (주소 검색 결과 선택 저장)",
            description = "Access Token(JWT) 필수. GET /regions/search로 검색한 regionCode를 저장합니다. 현재 위치 기반 자동 설정은 POST /location/user/cert를 사용하세요."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "설정 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 지역 코드",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MYPAGE_404_2",
                                    value = "{\"isSuccess\":false,\"code\":\"MYPAGE_404_2\",\"message\":\"존재하지 않는 지역 코드입니다.\",\"result\":null}"
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

package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.res.WithdrawalPreviewRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.WithdrawalService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "mypage",
        description = "마이페이지 API. 공지사항 목록/상세만 인증 불필요. 그 외 JWT Bearer 필수. "
                + "커서(nextCursor)는 opaque 값으로 다음 요청에 그대로 전달하세요."
)
@RestController
@RequestMapping("/mypage/withdrawal")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @Operation(
            summary = "탈퇴 전 확인 항목 조회",
            description = """
                    Access Token(JWT) 필수. 앱에서 탈퇴 확인 화면을 그릴 때 사용합니다.
                    - `subscribing`: BASIC이 아니면 true (유료 플랜 이용 중)
                    - `hasOngoingMarketTrade`: 장터 게시글 상태가 거래중(IN_PROGRESS) 또는 예약중(RESERVED)
                    - 서버는 이 값과 무관하게 탈퇴 API를 막지 않습니다. 안내는 클라이언트 책임입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_WITHDRAWAL_PREVIEW_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "확인 항목",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_WITHDRAWAL_PREVIEW_200",
                                              "message": "탈퇴 전 확인 정보 조회에 성공했습니다.",
                                              "result": {
                                                "subscribing": false,
                                                "subscribePlan": "BASIC",
                                                "hasOngoingMarketTrade": true
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
    @GetMapping("/preview")
    public ApiResponse<WithdrawalPreviewRes> preview() {
        return ApiResponse.onSuccess(MypageSuccessCode.WITHDRAWAL_PREVIEW_200, withdrawalService.preview());
    }

    @Operation(
            summary = "회원 탈퇴",
            description = """
                    Access Token(JWT) 필수. Soft delete입니다.
                    - 이름/소개/지역/프로필/푸시 토큰을 지우고 닉네임은 `탈퇴한 사용자`, 이메일은 익명화합니다.
                    - 이미 작성된 커뮤니티 글/댓글은 보존됩니다.
                    - refresh token은 전부 삭제되고, 프로필 이미지는 커밋 후 S3에서 삭제됩니다.
                    - 이후 로그인(로컬/카카오)은 LOCAL_LOGIN_401_2로 거절됩니다 (비밀번호 오류와 동일 코드).
                    - 이미 탈퇴한 토큰으로 재호출하면 LOGIN_LINK_400입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_WITHDRAWAL_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "탈퇴 완료",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_WITHDRAWAL_200",
                                              "message": "회원 탈퇴가 완료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "이미 탈퇴한 계정 등 (LOGIN_LINK_400)",
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
    @DeleteMapping
    public ApiResponse<Void> withdraw() {
        withdrawalService.withdraw();
        return ApiResponse.onSuccess(MypageSuccessCode.WITHDRAWAL_200, null);
    }
}

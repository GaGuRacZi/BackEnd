package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.res.WithdrawalPreviewRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.WithdrawalService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/mypage/withdrawal")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @Operation(
            summary = "탈퇴 전 확인 항목 조회",
            description = "Access Token(JWT) 필수. 구독 이용 중 여부, 진행중인 장터 거래 여부를 반환합니다."
    )
    @GetMapping("/preview")
    public ApiResponse<WithdrawalPreviewRes> preview() {
        return ApiResponse.onSuccess(MypageSuccessCode.WITHDRAWAL_PREVIEW_200, withdrawalService.preview());
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "Access Token(JWT) 필수. Soft delete로 처리되며 개인식별정보는 익명화됩니다. 이미 생성된 커뮤니티 글/댓글은 보존됩니다. 이미 탈퇴한 계정은 인증 오류로 거부됩니다."
    )
    @DeleteMapping
    public ApiResponse<Void> withdraw() {
        withdrawalService.withdraw();
        return ApiResponse.onSuccess(MypageSuccessCode.WITHDRAWAL_200, null);
    }
}

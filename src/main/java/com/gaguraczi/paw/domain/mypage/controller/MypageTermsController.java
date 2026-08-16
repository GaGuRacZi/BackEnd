package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.res.MyTermsRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.MypageTermsService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/mypage/terms")
@RequiredArgsConstructor
public class MypageTermsController {

    private final MypageTermsService mypageTermsService;

    @Operation(
            summary = "약관 목록 조회 (내 동의 상태 포함)",
            description = "Access Token(JWT) 필수. 약관 원문은 GET /terms/{type}을 그대로 사용하세요."
    )
    @GetMapping
    public ApiResponse<List<MyTermsRes>> list() {
        return ApiResponse.onSuccess(MypageSuccessCode.TERMS_LIST_200, mypageTermsService.list());
    }
}

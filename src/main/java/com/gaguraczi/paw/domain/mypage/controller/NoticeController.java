package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.res.NoticeDetailRes;
import com.gaguraczi.paw.domain.mypage.dto.res.NoticeListItemRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.NoticeService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/mypage/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회", description = "인증 불필요. keyword로 제목 검색, 커서 기반 페이지네이션. 등록 7일 이내는 isNew=true.")
    @GetMapping
    public ApiResponse<CursorPageRes<NoticeListItemRes>> search(
            @Parameter(description = "제목 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기, 기본 20 최대 50") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTICE_LIST_200, noticeService.search(keyword, cursor, size));
    }

    @Operation(summary = "공지사항 상세 조회", description = "인증 불필요. 조회 시 viewCount가 증가합니다.")
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDetailRes> getDetail(
            @Parameter(description = "공지사항 ID", example = "1") @PathVariable Long noticeId
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTICE_DETAIL_200, noticeService.getDetail(noticeId));
    }
}

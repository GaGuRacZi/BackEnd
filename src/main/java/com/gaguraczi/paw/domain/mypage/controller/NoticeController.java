package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.res.NoticeDetailRes;
import com.gaguraczi.paw.domain.mypage.dto.res.NoticeListItemRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.NoticeService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "mypage",
        description = "마이페이지 API. 공지사항 목록/상세만 인증 불필요. 그 외 JWT Bearer 필수. "
                + "커서(nextCursor)는 opaque 값으로 다음 요청에 그대로 전달하세요."
)
@RestController
@RequestMapping("/mypage/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
            summary = "공지사항 목록 조회",
            description = """
                    인증 불필요(permitAll). 최신순 커서 페이지네이션.
                    - keyword: 제목 부분 검색. 공백/미입력이면 전체
                    - 등록일 기준 7일 이내면 `isNew=true` (NEW 뱃지)
                    - size 기본 20, 최대 50. cursor는 이전 응답 nextCursor를 그대로 전달
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_NOTICE_LIST_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "공지 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_NOTICE_LIST_200",
                                              "message": "공지사항 목록 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "noticeId": 1,
                                                    "title": "서비스 점검 안내",
                                                    "isNew": true,
                                                    "createdAt": "2026-08-18T09:00:00"
                                                  }
                                                ],
                                                "nextCursor": "MjAyNi0wOC0xOFQwOTowMDowMHwx",
                                                "hasNext": true,
                                                "size": 20
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 커서 (MYPAGE_400)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MYPAGE_400",
                                    value = """
                                            {"isSuccess":false,"code":"MYPAGE_400","message":"요청 처리에 실패했습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<NoticeListItemRes>> search(
            @Parameter(description = "제목 검색어", example = "점검")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "이전 응답의 nextCursor", example = "MjAyNi0wOC0xOFQwOTowMDowMHwx")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTICE_LIST_200, noticeService.search(keyword, cursor, size));
    }

    @Operation(
            summary = "공지사항 상세 조회",
            description = """
                    인증 불필요(permitAll).
                    - 조회 시 viewCount가 1 증가한 뒤 반환됩니다.
                    - 없는 ID면 MYPAGE_404_3
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_NOTICE_DETAIL_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "공지 상세",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_NOTICE_DETAIL_200",
                                              "message": "공지사항 상세 조회에 성공했습니다.",
                                              "result": {
                                                "noticeId": 1,
                                                "title": "서비스 점검 안내",
                                                "content": "8월 21일 02:00~04:00 점검이 예정되어 있습니다.",
                                                "viewCount": 42,
                                                "createdAt": "2026-08-18T09:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공지 없음 (MYPAGE_404_3)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MYPAGE_404_3",
                                    value = """
                                            {"isSuccess":false,"code":"MYPAGE_404_3","message":"공지사항을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDetailRes> getDetail(
            @Parameter(description = "공지사항 ID", example = "1", required = true)
            @PathVariable Long noticeId
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTICE_DETAIL_200, noticeService.getDetail(noticeId));
    }
}

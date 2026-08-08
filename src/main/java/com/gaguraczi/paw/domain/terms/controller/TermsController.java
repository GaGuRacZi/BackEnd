package com.gaguraczi.paw.domain.terms.controller;

import com.gaguraczi.paw.domain.terms.dto.res.TermsDetailRes;
import com.gaguraczi.paw.domain.terms.dto.res.TermsSummaryRes;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import com.gaguraczi.paw.domain.terms.exception.code.TermsSuccessCode;
import com.gaguraczi.paw.domain.terms.service.TermsService;
import com.gaguraczi.paw.global.api.ApiResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "terms", description = "약관 API")
@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @Operation(
            summary = "약관 목록",
            description = "인증 불필요(permitAll). 현재 유효한 약관 요약 목록을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "TERMS_LIST_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "TERMS_LIST_200",
                                              "message": "약관 목록 조회에 성공했습니다.",
                                              "result": [
                                                {
                                                  "type": "AGE_OVER_14",
                                                  "title": "만 14세 이상 확인",
                                                  "version": "1.0",
                                                  "required": true,
                                                  "effectiveAt": "2025-01-01"
                                                },
                                                {
                                                  "type": "TERMS_OF_SERVICE",
                                                  "title": "서비스 이용약관",
                                                  "version": "1.0",
                                                  "required": true,
                                                  "effectiveAt": "2025-01-01"
                                                },
                                                {
                                                  "type": "MARKETING_PUSH",
                                                  "title": "마케팅 푸시 수신 동의",
                                                  "version": "1.0",
                                                  "required": false,
                                                  "effectiveAt": "2025-01-01"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<List<TermsSummaryRes>> list() {
        return ApiResponse.onSuccess(TermsSuccessCode.TERMS_LIST_200, termsService.list());
    }

    @Operation(
            summary = "약관 상세",
            description = "인증 불필요(permitAll). path의 TermsType으로 본문(content) 포함 상세를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "TERMS_DETAIL_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "TERMS_DETAIL_200",
                                              "message": "약관 상세 조회에 성공했습니다.",
                                              "result": {
                                                "type": "TERMS_OF_SERVICE",
                                                "title": "서비스 이용약관",
                                                "content": "제1조 (목적)\\n본 약관은 ...",
                                                "version": "1.0",
                                                "required": true,
                                                "effectiveAt": "2025-01-01"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "지원하지 않는 TermsType",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "COMMON_400",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON_400",
                                              "message": "잘못된 요청입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "TERMS_404",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "약관 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "TERMS_404",
                                              "message": "약관을 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{type}")
    public ApiResponse<TermsDetailRes> detail(
            @Parameter(
                    description = "약관 타입",
                    example = "TERMS_OF_SERVICE",
                    required = true
            )
            @PathVariable TermsType type
    ) {
        return ApiResponse.onSuccess(TermsSuccessCode.TERMS_DETAIL_200, termsService.detail(type));
    }
}

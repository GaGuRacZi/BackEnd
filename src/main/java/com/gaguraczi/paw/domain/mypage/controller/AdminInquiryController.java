package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.billing.controller.BillingApiDocs;
import com.gaguraczi.paw.domain.mypage.dto.req.InquiryAnswerReq;
import com.gaguraczi.paw.domain.mypage.dto.res.AdminInquiryRes;
import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.AdminInquiryService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "admin-inquiries", description = AdminInquiryApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    @Operation(summary = "문의 목록", description = AdminInquiryApiDocs.LIST_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (ADMIN_INQUIRY_LIST_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "문의 목록", value = AdminInquiryApiDocs.LIST_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 커서 (MYPAGE_400)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "MYPAGE_400", value = BillingApiDocs.MYPAGE_400_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "JWT_401_1", value = BillingApiDocs.JWT_401_1_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "JWT_403_2",
                                            summary = "유효하지 않은 token",
                                            value = BillingApiDocs.JWT_403_2_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "JWT_403_3",
                                            summary = "ADMIN 권한 없음",
                                            value = BillingApiDocs.JWT_403_3_EXAMPLE
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<AdminInquiryRes>> getInquiries(
            @Parameter(
                    description = "이전 응답의 nextCursor. opaque 값이며 해석하지 마세요.",
                    example = "MjAyNi0wOC0yMFQxMTowMDowMHwx"
            ) @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size,
            @Parameter(
                    description = "처리 상태 필터. 생략 시 전체.",
                    example = "RECEIVED"
            ) @RequestParam(required = false) InquiryStatus status,
            @Parameter(
                    description = "문의 유형 필터. 생략 시 전체.",
                    example = "PAYMENT"
            ) @RequestParam(required = false) InquiryType inquiryType
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.ADMIN_INQUIRY_LIST_200,
                adminInquiryService.getInquiries(cursor, size, status, inquiryType)
        );
    }

    @Operation(summary = "문의 상세", description = AdminInquiryApiDocs.DETAIL_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (ADMIN_INQUIRY_DETAIL_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "문의 상세", value = AdminInquiryApiDocs.DETAIL_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "JWT_401_1", value = BillingApiDocs.JWT_401_1_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "JWT_403_2",
                                            summary = "유효하지 않은 token",
                                            value = BillingApiDocs.JWT_403_2_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "JWT_403_3",
                                            summary = "ADMIN 권한 없음",
                                            value = BillingApiDocs.JWT_403_3_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의 없음 (MYPAGE_404_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "MYPAGE_404_1",
                                    value = AdminInquiryApiDocs.MYPAGE_404_1_EXAMPLE
                            )
                    )
            )
    })
    @GetMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryRes> getDetail(
            @Parameter(description = "문의 ID", example = "1", required = true) @PathVariable Long inquiryId
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.ADMIN_INQUIRY_DETAIL_200, adminInquiryService.getDetail(inquiryId));
    }

    @Operation(
            summary = "문의 답변",
            description = AdminInquiryApiDocs.ANSWER_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InquiryAnswerReq.class),
                            examples = @ExampleObject(
                                    name = "답변 등록",
                                    value = AdminInquiryApiDocs.ANSWER_REQ_EXAMPLE
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (ADMIN_INQUIRY_ANSWER_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "답변 완료", value = AdminInquiryApiDocs.ANSWER_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류 (COMMON_400)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "COMMON_400",
                                    value = AdminInquiryApiDocs.COMMON_400_ANSWER_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "JWT_401_1", value = BillingApiDocs.JWT_401_1_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "JWT_403_2",
                                            summary = "유효하지 않은 token",
                                            value = BillingApiDocs.JWT_403_2_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "JWT_403_3",
                                            summary = "ADMIN 권한 없음",
                                            value = BillingApiDocs.JWT_403_3_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의 없음 (MYPAGE_404_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "MYPAGE_404_1",
                                    value = AdminInquiryApiDocs.MYPAGE_404_1_EXAMPLE
                            )
                    )
            )
    })
    @PatchMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryRes> answer(
            @Parameter(description = "문의 ID", example = "1", required = true) @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerReq req
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.ADMIN_INQUIRY_ANSWER_200, adminInquiryService.answer(inquiryId, req));
    }
}

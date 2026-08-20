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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "admin-inquiries", description = "관리자 문의 API. ADMIN 역할 필요.")
@RestController
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    @Operation(
            summary = "문의 목록",
            description = "전체 유저 문의를 최신순 커서로 조회합니다. status, inquiryType은 선택 필터입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "ADMIN_INQUIRY_LIST_200"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(examples = @ExampleObject(value = BillingApiDocs.JWT_401_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = BillingApiDocs.JWT_403_3_DESCRIPTION,
                    content = @Content(examples = @ExampleObject(value = BillingApiDocs.JWT_403_3_EXAMPLE))
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<AdminInquiryRes>> getInquiries(
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50") @RequestParam(required = false) Integer size,
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) InquiryType inquiryType
    ) {
        return ApiResponse.onSuccess(
                MypageSuccessCode.ADMIN_INQUIRY_LIST_200,
                adminInquiryService.getInquiries(cursor, size, status, inquiryType)
        );
    }

    @Operation(summary = "문의 상세")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "ADMIN_INQUIRY_DETAIL_200"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "MYPAGE_404_1",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {"isSuccess":false,"code":"MYPAGE_404_1","message":"문의 내역을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryRes> getDetail(@PathVariable Long inquiryId) {
        return ApiResponse.onSuccess(MypageSuccessCode.ADMIN_INQUIRY_DETAIL_200, adminInquiryService.getDetail(inquiryId));
    }

    @Operation(summary = "문의 답변", description = "답변을 저장하고 status를 ANSWERED로 바꿉니다. 재답변은 덮어씁니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "ADMIN_INQUIRY_ANSWER_200"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MYPAGE_404_1")
    })
    @PatchMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryRes> answer(
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAnswerReq req
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.ADMIN_INQUIRY_ANSWER_200, adminInquiryService.answer(inquiryId, req));
    }
}

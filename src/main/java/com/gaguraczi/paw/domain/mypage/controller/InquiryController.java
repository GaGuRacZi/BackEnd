package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.req.InquiryCreateReq;
import com.gaguraczi.paw.domain.mypage.dto.res.InquiryRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.InquiryService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/mypage/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(
            summary = "문의 등록",
            description = "Access Token(JWT) 필수. multipart/form-data: data(JSON, 필수) + files(다중, 선택).",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = InquiryCreateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryRes> create(
            @RequestPart("data") @Valid InquiryCreateReq data,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.INQUIRY_CREATE_200, inquiryService.create(data, files));
    }

    @Operation(summary = "내 문의 내역 조회", description = "Access Token(JWT) 필수. 커서 기반 페이지네이션.")
    @GetMapping
    public ApiResponse<CursorPageRes<InquiryRes>> getMyInquiries(
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기, 기본 20 최대 50") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.INQUIRY_LIST_200, inquiryService.getMyInquiries(cursor, size));
    }

    @Operation(summary = "문의 상세 조회", description = "Access Token(JWT) 필수. 본인 문의만 조회 가능.")
    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryRes> getDetail(
            @Parameter(description = "문의 ID", example = "1") @PathVariable Long inquiryId
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.INQUIRY_DETAIL_200, inquiryService.getDetail(inquiryId));
    }

    @Schema(name = "InquiryCreateMultipart", description = "문의 등록 multipart")
    public static class InquiryCreateMultipart {
        @Schema(description = "문의 정보 JSON", implementation = InquiryCreateReq.class)
        public InquiryCreateReq data;

        @Schema(description = "첨부파일 (다중)", type = "array")
        public List<MultipartFile> files;
    }
}

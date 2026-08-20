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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(
        name = "mypage",
        description = "마이페이지 API. 공지사항 목록/상세만 인증 불필요. 그 외 JWT Bearer 필수. "
                + "커서(nextCursor)는 opaque 값으로 다음 요청에 그대로 전달하세요."
)
@RestController
@RequestMapping("/mypage/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(
            summary = "문의 등록",
            description = """
                    Access Token(JWT) 필수. multipart/form-data: `data`(JSON, 필수) + `files`(다중, 선택).
                    - inquiryType: ACCOUNT | PAYMENT | PET | COMMUNITY | ETC
                    - 등록 직후 status는 RECEIVED, answer는 null
                    - 빈 파일은 무시합니다. 업로드 실패/롤백 시 업로드된 S3 객체는 정리됩니다.
                    """,
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_INQUIRY_CREATE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "문의 등록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_INQUIRY_CREATE_200",
                                              "message": "문의가 등록되었습니다.",
                                              "result": {
                                                "inquiryId": 1,
                                                "inquiryType": "PAYMENT",
                                                "content": "구독 결제가 반복해서 실패해요.",
                                                "attachmentUrls": [
                                                  "https://cdn.example.com/inquiry/uuid.png"
                                                ],
                                                "status": "RECEIVED",
                                                "answer": null,
                                                "createdAt": "2026-08-20T11:00:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류 (COMMON_400)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "COMMON_400",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON_400",
                                              "message": "잘못된 요청입니다.",
                                              "result": { "inquiryType": "문의 유형은 필수입니다." }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<InquiryRes> create(
            @RequestPart("data") @Valid InquiryCreateReq data,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.INQUIRY_CREATE_200, inquiryService.create(data, files));
    }

    @Operation(
            summary = "내 문의 내역 조회",
            description = """
                    Access Token(JWT) 필수. 본인 문의만, 최신순 커서 페이지네이션.
                    - size 기본 20, 최대 50
                    - cursor는 이전 응답 nextCursor를 그대로 전달
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_INQUIRY_LIST_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "문의 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_INQUIRY_LIST_200",
                                              "message": "문의 내역 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "inquiryId": 1,
                                                    "inquiryType": "PAYMENT",
                                                    "content": "구독 결제가 반복해서 실패해요.",
                                                    "attachmentUrls": [],
                                                    "status": "ANSWERED",
                                                    "answer": "결제 내역을 확인했습니다. 재시도해 주세요.",
                                                    "createdAt": "2026-08-20T11:00:00"
                                                  }
                                                ],
                                                "nextCursor": null,
                                                "hasNext": false,
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
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "MYPAGE_400",
                                    value = """
                                            {"isSuccess":false,"code":"MYPAGE_400","message":"요청 처리에 실패했습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<InquiryRes>> getMyInquiries(
            @Parameter(description = "이전 응답의 nextCursor", example = "MjAyNi0wOC0yMFQxMTowMDowMHwx")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.INQUIRY_LIST_200, inquiryService.getMyInquiries(cursor, size));
    }

    @Operation(
            summary = "문의 상세 조회",
            description = """
                    Access Token(JWT) 필수. 본인 문의만 조회 가능합니다.
                    - 다른 사람 문의 ID를 넣으면 존재 여부와 무관하게 MYPAGE_404_1입니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_INQUIRY_DETAIL_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "문의 상세",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_INQUIRY_DETAIL_200",
                                              "message": "문의 상세 조회에 성공했습니다.",
                                              "result": {
                                                "inquiryId": 1,
                                                "inquiryType": "PAYMENT",
                                                "content": "구독 결제가 반복해서 실패해요.",
                                                "attachmentUrls": [
                                                  "https://cdn.example.com/inquiry/uuid.png"
                                                ],
                                                "status": "IN_PROGRESS",
                                                "answer": null,
                                                "createdAt": "2026-08-20T11:00:00"
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
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "문의 없음/타인 문의 (MYPAGE_404_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "MYPAGE_404_1",
                                    value = """
                                            {"isSuccess":false,"code":"MYPAGE_404_1","message":"문의 내역을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryRes> getDetail(
            @Parameter(description = "문의 ID", example = "1", required = true)
            @PathVariable Long inquiryId
    ) {
        return ApiResponse.onSuccess(MypageSuccessCode.INQUIRY_DETAIL_200, inquiryService.getDetail(inquiryId));
    }

    @Schema(name = "InquiryCreateMultipart", description = "문의 등록 multipart. data는 JSON, files는 선택 다중 파일")
    public static class InquiryCreateMultipart {
        @Schema(
                description = "문의 정보 JSON. 예: {\"inquiryType\":\"PAYMENT\",\"content\":\"구독 결제가 반복해서 실패해요.\"}",
                implementation = InquiryCreateReq.class
        )
        public InquiryCreateReq data;

        @Schema(description = "첨부파일 (다중, 선택). 빈 파일은 무시됩니다.", type = "array")
        public List<MultipartFile> files;
    }
}

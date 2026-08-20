package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.res.MyTermsRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.MypageTermsService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "mypage",
        description = "마이페이지 API. 공지사항 목록/상세만 인증 불필요. 그 외 JWT Bearer 필수. "
                + "커서(nextCursor)는 opaque 값으로 다음 요청에 그대로 전달하세요."
)
@RestController
@RequestMapping("/mypage/terms")
@RequiredArgsConstructor
public class MypageTermsController {

    private final MypageTermsService mypageTermsService;

    @Operation(
            summary = "약관 목록 조회 (내 동의 상태 포함)",
            description = """
                    Access Token(JWT) 필수.
                    - 현재 등록된 약관 목록에 `agreed`(해당 type+version 동의 여부)를 붙입니다.
                    - 필수 약관이 앞에 오고, 그다음 type 순입니다.
                    - 약관 원문(content)은 `GET /terms/{type}`을 사용하세요.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_TERMS_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "약관 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_TERMS_200",
                                              "message": "약관 목록 조회에 성공했습니다.",
                                              "result": [
                                                {
                                                  "type": "AGE_OVER_14",
                                                  "title": "만 14세 이상 확인",
                                                  "version": "1.0",
                                                  "required": true,
                                                  "effectiveAt": "2025-01-01",
                                                  "agreed": true
                                                },
                                                {
                                                  "type": "TERMS_OF_SERVICE",
                                                  "title": "서비스 이용약관",
                                                  "version": "1.0",
                                                  "required": true,
                                                  "effectiveAt": "2025-01-01",
                                                  "agreed": true
                                                },
                                                {
                                                  "type": "MARKETING_PUSH",
                                                  "title": "마케팅 푸시 수신 동의",
                                                  "version": "1.0",
                                                  "required": false,
                                                  "effectiveAt": "2025-01-01",
                                                  "agreed": false
                                                }
                                              ]
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
    @GetMapping
    public ApiResponse<List<MyTermsRes>> list() {
        return ApiResponse.onSuccess(MypageSuccessCode.TERMS_LIST_200, mypageTermsService.list());
    }
}

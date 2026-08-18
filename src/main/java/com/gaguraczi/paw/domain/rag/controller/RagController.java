package com.gaguraczi.paw.domain.rag.controller;

import com.gaguraczi.paw.domain.rag.dto.RagSearchQuery;
import com.gaguraczi.paw.domain.rag.dto.res.RagAskRes;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import com.gaguraczi.paw.domain.rag.exception.code.RagSuccessCode;
import com.gaguraczi.paw.domain.rag.service.RagSearchService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "rag", description = "반려견 지식 RAG 질의 API")
@RestController
@RequestMapping("/rag")
@Validated
@RequiredArgsConstructor
public class RagController {

    private final RagSearchService ragSearchService;

    @Operation(
            summary = "지식 질의",
            description = "JWT 필수. OpenAI 모델이 벡터 저장소를 file_search로 직접 조회한 뒤 답합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "RAG_SEARCH_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "RAG_SEARCH_200",
                                              "message": "지식 질의에 성공했습니다.",
                                              "result": {
                                                "answer": "노령견 관절염이면 다리를 절 수 있어요. 확진·처방은 병원에서 받으세요.",
                                                "sources": [
                                                  {
                                                    "sourceId": "DR-001-0001",
                                                    "chunkIndex": 0,
                                                    "sourceType": "QA",
                                                    "department": "내과",
                                                    "lifeCycle": "노령견",
                                                    "disease": "관절염",
                                                    "title": null,
                                                    "content": "[질문]\\n앞다리를 절어요",
                                                    "score": 0.87
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<RagAskRes> ask(
            @Parameter(description = "질문", example = "앞다리를 절어요", required = true)
            @NotBlank
            @RequestParam String q,
            @Parameter(description = "모델이 참고할 최대 검색 결과 수", example = "8")
            @Max(RagSearchService.MAX_RESULTS)
            @RequestParam(required = false) Integer topK,
            @Parameter(description = "출처 유형", example = "QA")
            @RequestParam(required = false) RagSourceType sourceType,
            @Parameter(description = "과목", example = "내과")
            @RequestParam(required = false) String department,
            @Parameter(description = "생애주기", example = "노령견")
            @RequestParam(required = false) String lifeCycle
    ) {
        return ApiResponse.onSuccess(
                RagSuccessCode.RAG_SEARCH_200,
                RagAskRes.from(ragSearchService.ask(
                        new RagSearchQuery(q, topK, sourceType, department, lifeCycle)))
        );
    }
}

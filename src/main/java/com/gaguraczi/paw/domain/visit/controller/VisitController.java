package com.gaguraczi.paw.domain.visit.controller;

import com.gaguraczi.paw.domain.visit.dto.req.VisitCreateReq;
import com.gaguraczi.paw.domain.visit.dto.req.VisitPrescriptionAddReq;
import com.gaguraczi.paw.domain.visit.dto.res.VisitAiSummaryRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitCreateRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitDetailRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitListRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitPrescriptionRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitTranscriptRes;
import com.gaguraczi.paw.domain.visit.exception.code.VisitSuccessCode;
import com.gaguraczi.paw.domain.visit.service.VisitService;
import com.gaguraczi.paw.global.api.ApiResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
        name = "visits",
        description = """
                반려동물 진료 녹음 → 전사 → 짧은 요약 → 처방 → 코인 AI 상세 요약 API입니다.
                별도 명세 없이 이 태그와 각 엔드포인트 description·Responses 예시만으로 화면을 붙일 수 있습니다.
                
                ## 권한
                - 모든 엔드포인트 JWT 필수 (`Authorization: Bearer {accessToken}`).
                - 본인 펫/진료만 접근합니다. 남의 펫은 `PET_404`, 남의 진료·없는 진료는 `VISIT_404` (존재 여부를 숨깁니다).
                
                ## 화면 순서
                1. 녹음 업로드 `POST /visits` → 즉시 `PROCESSING` 카드를 그립니다.
                2. `GET /visits?petId=` 또는 `GET /visits/{visitId}` 를 폴링하거나, 아래 FCM으로 완료를 기다립니다.
                3. `READY`가 되면 요약 상세·전사문·처방을 열고, 필요하면 AI 상세 요약을 요청합니다.
                4. `FAILED`면 `failReason`을 보여주고 다시 녹음 업로드합니다. 재처리 API는 없습니다.
                
                ## status (VisitStatus)
                | 값 | 의미 | 클라이언트 |
                |---|---|---|
                | PROCESSING | STT·짧은 요약 비동기 중 | 제목/한줄요약은 null. 스피너. 전사문·처방추가·AI요약 호출 금지 |
                | READY | 짧은 요약·전사문 준비됨 | 카드/상세 본문 표시. 전사문·처방·AI요약 가능 |
                | FAILED | STT 또는 짧은 요약 실패 | 상세 `failReason`. 새로 업로드 |
                
                ## aiSummaryStatus (짧은 요약과 별개)
                | 값 | 의미 |
                |---|---|
                | NONE | 코인 AI 상세 요약을 아직 안 만듦 |
                | GENERATING | 생성 중. 같은 진료 재요청은 `VISIT_409` |
                | DONE | 저장됨. 이후 POST는 재과금 없이 같은 마크다운 반환 |
                
                ## 비동기 완료 알림 (FCM)
                푸시 토큰이 있는 기기만 발송합니다. data payload:
                - `type`: `VISIT_READY` 또는 `VISIT_FAILED`
                - `visitId`, `petId`: 문자열
                - 알림 문구 예: 제목 `진료 요약` / 본문 `AI 진료 요약이 완료되었어요.` 또는 `진료 요약 생성에 실패했어요.`
                
                ## 코인
                - 잔액은 `GET /users/me` 의 `coin`, `usedCoin`.
                - `POST /visits/{visitId}/ai-summary` 만 코인 1개를 씁니다. 짧은 요약은 무료입니다.
                - 생성 실패 시 차감분을 환불합니다. 이미 DONE이면 과금하지 않습니다.
                
                ## 관련 API
                - 마스터 약 검색: `GET /medications?q=`
                - 약 설명/주의: `GET /medications/{medicationId}` (CATALOG 처방의 caution 기본값 출처)
                """
)
@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @Operation(
            summary = "진료 녹음 업로드",
            description = """
                    진료실 녹음을 올려 진료 카드를 만듭니다. 응답이 오는 시점에 STT는 아직 끝나 있지 않습니다.
                    
                    ## Request (multipart/form-data)
                    | part | Content-Type | 필수 | 내용 |
                    |---|---|---|---|
                    | data | application/json | 예 | `{ "petId": 1 }` 만 보냅니다. 진료명·병원명은 보내지 않습니다. |
                    | audio | 파일 | 예 | mp3 / m4a / aac. **wav 불가**. 최대 **100MB**, **60분**. |
                    
                    ## 업로드 직후 서버 동작
                    1. 본인 펫인지 확인 → 음성 검사 → S3 `visit-audio/` 저장
                    2. DB에 `status=PROCESSING` 진료 생성 후 **이 API는 바로 200**
                    3. 커밋 이후 비동기로 STT(화자 분리) → 의사/보호자 매핑 → 짧은 요약(진료명, 한줄요약, 진단, 케어, 병원명)
                    4. 성공 시 `READY` + FCM `VISIT_READY`, 실패 시 `FAILED` + FCM `VISIT_FAILED`
                    
                    ## 클라이언트
                    - 로딩 화면에서 이 API를 호출하고, 받은 `visitId`로 목록/상세를 폴링하세요.
                    - 대용량 파일이라 게이트웨이 idle timeout은 약 600초입니다. 클라이언트 HTTP timeout도 넉넉히 잡으세요.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = VisitCreateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "audio", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = @ExampleObject(
                                    name = "data JSON",
                                    value = """
                                            {
                                              "petId": 1
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_CREATE_200. 등록됨. status는 항상 PROCESSING.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_CREATE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "VISIT_CREATE_200",
                                              "message": "진료 기록이 등록되었습니다.",
                                              "result": {
                                                "visitId": 1,
                                                "petId": 1,
                                                "status": "PROCESSING"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = """
                            VISIT_400 음성 없음 / VISIT_400_PET petId 없음 / VISIT_400_AUDIO_TYPE wav·미지원 포맷 \
                            / VISIT_400_AUDIO_TOO_LARGE 100MB 초과 / VISIT_400_AUDIO_DURATION 60분 초과 / COMMON_400 유효성
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "VISIT_400",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_400","message":"음성 파일이 필요합니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_PET",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_400_PET","message":"펫 ID가 필요합니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_AUDIO_TYPE",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_400_AUDIO_TYPE","message":"mp3, m4a, aac 파일만 업로드할 수 있습니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_AUDIO_TOO_LARGE",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_400_AUDIO_TOO_LARGE","message":"음성 파일은 100MB 이하여야 합니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_AUDIO_DURATION",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_400_AUDIO_DURATION","message":"녹음은 최대 60분까지 가능합니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1. 토큰 만료 또는 미인증.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "JWT_403_2. 유효하지 않은 토큰.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_404. 펫이 없거나 본인 소유가 아님.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_404",
                                    value = """
                                            {"isSuccess":false,"code":"PET_404","message":"펫을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VisitCreateRes> create(
            @RequestPart("data") @Valid VisitCreateReq data,
            @RequestPart("audio") MultipartFile audio
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_CREATE_200, visitService.create(data, audio));
    }

    @Operation(
            summary = "진료 목록",
            description = """
                    한 펫의 진료 카드 목록입니다. 최신 업로드가 먼저 옵니다 (`visitedAt` = createdAt).
                    
                    ## Query
                    - `petId` (필수): 본인 펫 ID. 남의 펫이면 PET_404.
                    
                    ## result[] 필드
                    | 필드 | PROCESSING/FAILED | READY |
                    |---|---|---|
                    | visitId | 있음 | 있음 |
                    | visitedAt | 업로드 시각 | 동일 |
                    | status | PROCESSING 또는 FAILED | READY |
                    | visitName | **null** | 짧은 요약 진료명 |
                    | oneLineSummary | **null** | 한 줄 요약 |
                    | aiSummaryGenerated | 보통 false | 코인 AI 요약 DONE이면 true |
                    
                    폴링 시 `status`가 PROCESSING이면 스피너, READY면 제목/한줄요약을 채우고, FAILED면 실패 카드로 바꾸세요.
                    실패 사유는 목록에 없고 `GET /visits/{visitId}` 의 `failReason`에 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_LIST_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "READY와 PROCESSING 혼재",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_LIST_200",
                                                      "message": "진료 목록 조회에 성공했습니다.",
                                                      "result": [
                                                        {
                                                          "visitId": 2,
                                                          "visitedAt": "2026-08-19T13:40:00",
                                                          "visitName": null,
                                                          "status": "PROCESSING",
                                                          "aiSummaryGenerated": false,
                                                          "oneLineSummary": null
                                                        },
                                                        {
                                                          "visitId": 1,
                                                          "visitedAt": "2026-08-18T10:15:00",
                                                          "visitName": "스케일링",
                                                          "status": "READY",
                                                          "aiSummaryGenerated": false,
                                                          "oneLineSummary": "치석 제거와 잇몸 관리 안내를 받았어요."
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "FAILED 카드",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_LIST_200",
                                                      "message": "진료 목록 조회에 성공했습니다.",
                                                      "result": [
                                                        {
                                                          "visitId": 3,
                                                          "visitedAt": "2026-08-19T09:00:00",
                                                          "visitName": null,
                                                          "status": "FAILED",
                                                          "aiSummaryGenerated": false,
                                                          "oneLineSummary": null
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1. 토큰 만료 또는 미인증.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "JWT_403_2. 유효하지 않은 토큰.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_404",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_404",
                                    value = """
                                            {"isSuccess":false,"code":"PET_404","message":"펫을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<List<VisitListRes>> list(
            @Parameter(description = "본인 펫 ID", example = "1", required = true)
            @RequestParam Long petId
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_LIST_200, visitService.list(petId));
    }

    @Operation(
            summary = "진료 요약 상세",
            description = """
                    진료 요약 화면 한 장입니다. 전사문 원문은 포함하지 않습니다 (`GET /visits/{visitId}/transcript`).
                    
                    ## 상태별 본문
                    - **PROCESSING**: pet 프로필·audioUrl은 있고, visitName/oneLineSummary/diagnosisFindings/careItems/careNote/aiSummaryMd는 비움.
                    - **READY**: 짧은 요약 필드가 채워집니다. 처방은 사용자가 추가한 목록입니다(자동 추출 없음).
                    - **FAILED**: 본문은 PROCESSING과 같고 `failReason`이 있습니다.
                    
                    ## 주요 필드
                    - `petAgeLabel`: `3살 2개월` 또는 `5개월`. 생일 없으면 null.
                    - `aiSummaryStatus`: NONE / GENERATING / DONE. DONE이면 `aiSummaryMd`에 저장된 상세 요약이 있습니다.
                    - `prescriptions`: 이 진료에 수동 추가한 약. OCR 없음.
                    
                    AI 상세 요약 버튼: `aiSummaryStatus==NONE` 이고 `status==READY` 일 때 활성화. 잔액은 `GET /users/me`.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_GET_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "READY",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_GET_200",
                                                      "message": "진료 조회에 성공했습니다.",
                                                      "result": {
                                                        "visitId": 1,
                                                        "status": "READY",
                                                        "visitedAt": "2026-08-18T10:15:00",
                                                        "visitName": "스케일링",
                                                        "petId": 1,
                                                        "petName": "초코",
                                                        "breedName": "말티즈",
                                                        "petAgeLabel": "3살 2개월",
                                                        "petProfileUrl": "https://cdn.example.com/pets/1.jpg",
                                                        "diagnosisFindings": ["치석이 많아요", "잇몸 염증이 있어요"],
                                                        "oneLineSummary": "치석 제거와 잇몸 관리 안내를 받았어요.",
                                                        "prescriptions": [
                                                          {
                                                            "prescriptionId": 10,
                                                            "source": "CATALOG",
                                                            "medicationId": 1,
                                                            "nameKo": "카미녹스",
                                                            "nameEn": "Carprofen 25mg",
                                                            "ingredient": "카르프로펜",
                                                            "dosageAmount": 1,
                                                            "dosageUnit": "정",
                                                            "frequency": "TWICE_DAILY",
                                                            "mealTiming": "AFTER_MEAL",
                                                            "takeTimes": ["MORNING", "EVENING"],
                                                            "caution": "위장 장애가 있으면 수의사와 상담하세요."
                                                          }
                                                        ],
                                                        "careItems": ["일주일 동안 딱딱한 간식은 피해주세요"],
                                                        "careNote": "잇몸에서 피가 나면 병원으로 연락하세요.",
                                                        "aiSummaryStatus": "NONE",
                                                        "aiSummaryMd": null,
                                                        "audioUrl": "https://cdn.example.com/visit-audio/xxx.m4a",
                                                        "failReason": null
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PROCESSING",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_GET_200",
                                                      "message": "진료 조회에 성공했습니다.",
                                                      "result": {
                                                        "visitId": 2,
                                                        "status": "PROCESSING",
                                                        "visitedAt": "2026-08-19T13:40:00",
                                                        "visitName": null,
                                                        "petId": 1,
                                                        "petName": "초코",
                                                        "breedName": "말티즈",
                                                        "petAgeLabel": "3살 2개월",
                                                        "petProfileUrl": "https://cdn.example.com/pets/1.jpg",
                                                        "diagnosisFindings": [],
                                                        "oneLineSummary": null,
                                                        "prescriptions": [],
                                                        "careItems": [],
                                                        "careNote": null,
                                                        "aiSummaryStatus": "NONE",
                                                        "aiSummaryMd": null,
                                                        "audioUrl": "https://cdn.example.com/visit-audio/yyy.m4a",
                                                        "failReason": null
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "FAILED",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_GET_200",
                                                      "message": "진료 조회에 성공했습니다.",
                                                      "result": {
                                                        "visitId": 3,
                                                        "status": "FAILED",
                                                        "visitedAt": "2026-08-19T09:00:00",
                                                        "visitName": null,
                                                        "petId": 1,
                                                        "petName": "초코",
                                                        "breedName": "말티즈",
                                                        "petAgeLabel": "3살 2개월",
                                                        "petProfileUrl": "https://cdn.example.com/pets/1.jpg",
                                                        "diagnosisFindings": [],
                                                        "oneLineSummary": null,
                                                        "prescriptions": [],
                                                        "careItems": [],
                                                        "careNote": null,
                                                        "aiSummaryStatus": "NONE",
                                                        "aiSummaryMd": null,
                                                        "audioUrl": "https://cdn.example.com/visit-audio/zzz.m4a",
                                                        "failReason": "음성 전사에 실패했습니다."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1. 토큰 만료 또는 미인증.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "JWT_403_2. 유효하지 않은 토큰.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "VISIT_404. 없거나 본인 진료가 아님.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_404","message":"진료 기록을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{visitId}")
    public ApiResponse<VisitDetailRes> get(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_GET_200, visitService.get(visitId));
    }

    @Operation(
            summary = "음성 전사문",
            description = """
                    의사/보호자로 나뉜 대화 턴입니다. **status=READY일 때만** 호출하세요.
                    PROCESSING·FAILED에서 호출하면 `VISIT_400_NOT_READY` 입니다.
                    
                    ## result
                    - `hospitalName`: 짧은 요약이 전사에서 뽑은 병원명. 없으면 null.
                    - `durationSec`: 재생 길이(초).
                    - `audioUrl`: 원본 녹음.
                    - `turns[]`: `speaker` 는 `VET`(수의사) 또는 `OWNER`(보호자). `startSec`/`endSec` 는 초 단위(없으면 null).
                    
                    화자 매핑은 서버가 수행하므로 클라이언트는 VET/OWNER만 구분하면 됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_TRANSCRIPT_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_TRANSCRIPT_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "VISIT_TRANSCRIPT_200",
                                              "message": "전사문 조회에 성공했습니다.",
                                              "result": {
                                                "visitId": 1,
                                                "hospitalName": "OO동물병원",
                                                "visitedAt": "2026-08-18T10:15:00",
                                                "audioUrl": "https://cdn.example.com/visit-audio/xxx.m4a",
                                                "durationSec": 780,
                                                "turns": [
                                                  {
                                                    "speaker": "VET",
                                                    "text": "오늘은 스케일링을 진행했어요.",
                                                    "startSec": 12.4,
                                                    "endSec": 18.1
                                                  },
                                                  {
                                                    "speaker": "OWNER",
                                                    "text": "집에서 이빨은 어떻게 닦아 주면 될까요?",
                                                    "startSec": 18.5,
                                                    "endSec": 22.0
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "VISIT_400_NOT_READY. 아직 PROCESSING이거나 FAILED.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_400_NOT_READY",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_400_NOT_READY","message":"진료 요약이 아직 준비되지 않았습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1. 토큰 만료 또는 미인증.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "JWT_403_2. 유효하지 않은 토큰.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "VISIT_404. 없거나 본인 진료가 아님.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_404","message":"진료 기록을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{visitId}/transcript")
    public ApiResponse<VisitTranscriptRes> transcript(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_TRANSCRIPT_200, visitService.transcript(visitId));
    }

    @Operation(
            summary = "처방 약물 추가",
            description = """
                    이 진료의 처방 목록에 약을 한 건 추가합니다. **status=READY일 때만** 가능합니다.
                    처방전 OCR·자동 저장은 없습니다. 사용자가 검색 선택 또는 직접 입력합니다.
                    
                    ## 공통 필수 JSON 필드
                    `source`, `frequency`, `mealTiming` (세 값 중 하나라도 없으면 `VISIT_400_PRESCRIPTION`)
                    
                    ## source=CATALOG (마스터에서 고름)
                    1. `GET /medications?q=카미녹스` 로 검색
                    2. 고른 `medicationId`를 넣고 호출
                    3. nameKo/nameEn/ingredient는 마스터 값으로 덮어씁니다(요청 값은 무시)
                    4. `caution`을 안 보내면 마스터 `precautionMd`의 첫 줄이 들어갑니다
                    5. 없는 medicationId → `MEDICATION_404`
                    
                    ## source=CUSTOM (기타)
                    - `nameKo` 필수. nameEn·ingredient·caution은 선택.
                    
                    ## 선택 필드
                    - `dosageAmount`: 숫자. 생략 가능
                    - `dosageUnit`: 생략 시 `"정"`
                    - `takeTimes`: `MORNING` `LUNCH` `EVENING` `BEDTIME` 배열. 생략 시 `[]`
                    
                    frequency: `ONCE_DAILY` | `TWICE_DAILY` | `THREE_TIMES` | `AS_NEEDED`  
                    mealTiming: `BEFORE_MEAL` | `AFTER_MEAL` | `BETWEEN_MEALS` | `ANYTIME`
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitPrescriptionAddReq.class),
                            examples = {
                                    @ExampleObject(
                                            name = "CATALOG",
                                            value = """
                                                    {
                                                      "source": "CATALOG",
                                                      "medicationId": 1,
                                                      "dosageAmount": 1,
                                                      "dosageUnit": "정",
                                                      "frequency": "TWICE_DAILY",
                                                      "mealTiming": "AFTER_MEAL",
                                                      "takeTimes": ["MORNING", "EVENING"]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "CUSTOM",
                                            value = """
                                                    {
                                                      "source": "CUSTOM",
                                                      "nameKo": "관절영양제",
                                                      "nameEn": "Glucosamine",
                                                      "ingredient": "글루코사민",
                                                      "dosageAmount": 1,
                                                      "dosageUnit": "포",
                                                      "frequency": "ONCE_DAILY",
                                                      "mealTiming": "ANYTIME",
                                                      "takeTimes": ["MORNING"],
                                                      "caution": "사료에 섞어 주세요."
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_PRESCRIPTION_ADD_200. 추가된 한 건을 반환합니다. 목록 전체는 GET 상세의 prescriptions.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "CATALOG 추가",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_PRESCRIPTION_ADD_200",
                                                      "message": "약물이 추가되었습니다.",
                                                      "result": {
                                                        "prescriptionId": 10,
                                                        "source": "CATALOG",
                                                        "medicationId": 1,
                                                        "nameKo": "카미녹스",
                                                        "nameEn": "Carprofen 25mg",
                                                        "ingredient": "카르프로펜",
                                                        "dosageAmount": 1,
                                                        "dosageUnit": "정",
                                                        "frequency": "TWICE_DAILY",
                                                        "mealTiming": "AFTER_MEAL",
                                                        "takeTimes": ["MORNING", "EVENING"],
                                                        "caution": "위장 장애가 있으면 수의사와 상담하세요."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "CUSTOM 추가",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_PRESCRIPTION_ADD_200",
                                                      "message": "약물이 추가되었습니다.",
                                                      "result": {
                                                        "prescriptionId": 11,
                                                        "source": "CUSTOM",
                                                        "medicationId": null,
                                                        "nameKo": "관절영양제",
                                                        "nameEn": "Glucosamine",
                                                        "ingredient": "글루코사민",
                                                        "dosageAmount": 1,
                                                        "dosageUnit": "포",
                                                        "frequency": "ONCE_DAILY",
                                                        "mealTiming": "ANYTIME",
                                                        "takeTimes": ["MORNING"],
                                                        "caution": "사료에 섞어 주세요."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "VISIT_400_NOT_READY 또는 VISIT_400_PRESCRIPTION (source/frequency/mealTiming 누락, CATALOG인데 medicationId 없음, CUSTOM인데 nameKo 없음)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "VISIT_400_NOT_READY",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_400_NOT_READY","message":"진료 요약이 아직 준비되지 않았습니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_PRESCRIPTION",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_400_PRESCRIPTION","message":"약물 정보가 올바르지 않습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1. 토큰 만료 또는 미인증.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "JWT_403_2. 유효하지 않은 토큰.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "VISIT_404 또는 MEDICATION_404(CATALOG 마스터 없음)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "VISIT_404",
                                            value = """
                                                    {"isSuccess":false,"code":"VISIT_404","message":"진료 기록을 찾을 수 없습니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "MEDICATION_404",
                                            value = """
                                                    {"isSuccess":false,"code":"MEDICATION_404","message":"약물을 찾을 수 없습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/{visitId}/medications")
    public ApiResponse<VisitPrescriptionRes> addPrescription(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId,
            @org.springframework.web.bind.annotation.RequestBody @Valid VisitPrescriptionAddReq req
    ) {
        return ApiResponse.onSuccess(
                VisitSuccessCode.VISIT_PRESCRIPTION_ADD_200,
                visitService.addPrescription(visitId, req)
        );
    }

    @Operation(
            summary = "처방 약물 삭제",
            description = """
                    이 진료에 속한 처방 한 건을 삭제합니다. CATALOG·CUSTOM 모두 가능합니다.
                    `prescriptionId`가 없거나 다른 진료의 약이면 `VISIT_404`.
                    성공 시 `result`는 null 입니다. 화면에서는 해당 행만 지우면 됩니다.
                    READY가 아니어도 호출은 되지만, 보통 처방은 READY 이후에만 존재합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_PRESCRIPTION_DELETE_200. result는 null.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_PRESCRIPTION_DELETE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "VISIT_PRESCRIPTION_DELETE_200",
                                              "message": "약물이 삭제되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1. 토큰 만료 또는 미인증.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "JWT_403_2. 유효하지 않은 토큰.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "VISIT_404. 없거나 본인 진료가 아님.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_404","message":"진료 기록을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/{visitId}/medications/{prescriptionId}")
    public ApiResponse<Void> deletePrescription(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId,
            @Parameter(description = "처방 ID (추가 응답의 prescriptionId)", example = "10", required = true)
            @PathVariable Long prescriptionId
    ) {
        visitService.deletePrescription(visitId, prescriptionId);
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_PRESCRIPTION_DELETE_200, null);
    }

    @Operation(
            summary = "AI 상세 요약 생성",
            description = """
                    전사문·처방·지식검색(file_search)을 근거로 **1000~1500자** 한국어 해요체 마크다운을 만듭니다.
                    짧은 요약(`oneLineSummary`)과 다른 유료 기능입니다.
                    
                    ## 과금
                    - 비용: 코인 **1** (`GET /users/me`의 coin에서 차감, usedCoin +1)
                    - `aiSummaryStatus`가 이미 **DONE**이면 저장된 마크다운을 **재과금 없이** 반환
                    - 생성 실패 시 코인 환불 후 에러 (`VISIT_502_2` 등)
                    - 잔액 부족: HTTP **402** `VISIT_402_COIN`
                    - 다른 요청이 생성 중: HTTP **409** `VISIT_409`
                    - 아직 READY 아님: `VISIT_400_NOT_READY`
                    
                    ## sources
                    - 이번 생성에서 지식 검색에 쓰인 출처 배열입니다. 화면에 각주로 쓸 수 있습니다.
                    - 이미 DONE인 요약을 다시 받으면 출처를 DB에 안 남기므로 `sources: []`
                    - 서버 `.env` `VISIT_AI_SUMMARY_INCLUDE_SOURCES=false` 이면 **필드 자체가 없음**
                    - 지식검색 자체는 끄지 않습니다. 응답에 출처만 숨깁니다.
                    
                    ## 버튼 노출
                    `status==READY` && `aiSummaryStatus==NONE` && `coin >= 1`
                    GENERATING이면 로딩. DONE이면 저장된 `aiSummaryMd`를 상세에서 보여 주거나 이 API로 다시 받습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_AI_SUMMARY_200. coin/usedCoin은 이 요청 이후 잔액.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "첫 생성 (sources 포함)",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_AI_SUMMARY_200",
                                                      "message": "AI 진료 요약을 생성했습니다.",
                                                      "result": {
                                                        "visitId": 1,
                                                        "aiSummaryMd": "## 오늘 진료\\n스케일링을 진행했어요.\\n\\n이 요약은 진료 기록을 돕기 위한 것이며 수의사 진단을 대신하지 않습니다.",
                                                        "coin": 9,
                                                        "usedCoin": 1,
                                                        "sources": [
                                                          {
                                                            "sourceId": "SRC-1",
                                                            "chunkIndex": 0,
                                                            "sourceType": "QA",
                                                            "department": "치과",
                                                            "lifeCycle": "성견",
                                                            "disease": "치석",
                                                            "title": null,
                                                            "content": "스케일링 후 잇몸 관리...",
                                                            "score": 0.91,
                                                            "fileName": "치과_QA_000.md"
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "이미 DONE (재과금 없음, sources 빈 배열)",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_AI_SUMMARY_200",
                                                      "message": "AI 진료 요약을 생성했습니다.",
                                                      "result": {
                                                        "visitId": 1,
                                                        "aiSummaryMd": "## 오늘 진료\\n스케일링을 진행했어요.",
                                                        "coin": 9,
                                                        "usedCoin": 1,
                                                        "sources": []
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "sources 숨김 설정",
                                            description = "VISIT_AI_SUMMARY_INCLUDE_SOURCES=false 이면 sources 키 없음",
                                            value = """
                                                    {
                                                      "isSuccess": true,
                                                      "code": "VISIT_AI_SUMMARY_200",
                                                      "message": "AI 진료 요약을 생성했습니다.",
                                                      "result": {
                                                        "visitId": 1,
                                                        "aiSummaryMd": "## 오늘 진료\\n스케일링을 진행했어요.",
                                                        "coin": 9,
                                                        "usedCoin": 1
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "VISIT_400_NOT_READY",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_400_NOT_READY",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_400_NOT_READY","message":"진료 요약이 아직 준비되지 않았습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1. 토큰 만료 또는 미인증.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "402",
                    description = "VISIT_402_COIN. HTTP 402. 잔액 부족.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_402_COIN",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_402_COIN","message":"코인이 부족합니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "JWT_403_2. 유효하지 않은 토큰.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "VISIT_404. 없거나 본인 진료가 아님.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_404","message":"진료 기록을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "VISIT_409. 같은 진료의 AI 요약이 이미 GENERATING.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_409",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_409","message":"AI 요약을 생성 중입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "VISIT_502_2 생성 실패(코인 환불됨). 글자 수 범위 미달 등.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_502_2",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_502_2","message":"AI 진료 요약 생성에 실패했습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "VISIT_503. 지식 검색 벡터스토어 미설정.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_503",
                                    value = """
                                            {"isSuccess":false,"code":"VISIT_503","message":"지식 검색 서비스를 일시적으로 사용할 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{visitId}/ai-summary")
    public ApiResponse<VisitAiSummaryRes> generateAiSummary(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId
    ) {
        return ApiResponse.onSuccess(
                VisitSuccessCode.VISIT_AI_SUMMARY_200,
                visitService.generateAiSummary(visitId)
        );
    }

    @Schema(name = "VisitCreateMultipart", description = "진료 녹음 업로드 multipart. data는 JSON 문자열, audio는 파일입니다.")
    public static class VisitCreateMultipart {
        @Schema(
                description = "JSON. petId만 필요합니다. 예: {\"petId\":1}",
                implementation = VisitCreateReq.class,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        public VisitCreateReq data;

        @Schema(
                description = "진료 음성. mp3/m4a/aac, 최대 100MB·60분. wav 불가.",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        public MultipartFile audio;
    }
}

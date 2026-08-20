package com.gaguraczi.paw.domain.weights.controller;

/**
 * Swagger annotation constants for {@link PetWeightController}. Documentation only.
 */
public final class PetWeightApiDocs {

    private PetWeightApiDocs() {
    }

    public static final String JWT_401_1_DESCRIPTION = "JWT_401_1. 토큰 만료 또는 미인증.";
    public static final String JWT_401_1_EXAMPLE = """
            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
            """;

    public static final String TAG_DESCRIPTION = """
            건강요약 - 체중 API입니다. JWT Bearer 필수. 본인 펫만 가능. 경로 prefix는 `/pets/{petId}/weights` 입니다. (`/api` 없음)
            
            ## 화면 구성
            1. 상단 카드: `GET /pets/{petId}/weights/summary` → 현재 체중 + 이번 달 증감
            2. 그래프: `GET /pets/{petId}/weights/graph?period=ONE_MONTH|SIX_MONTHS`
            3. 월별 목록: `GET /pets/{petId}/weights?year=&month=`
            4. 작성/수정은 **multipart/form-data** (`data` JSON + `images` 파일)
            
            ## 사진 규칙
            - 기록당 최대 3장, 파일당 5MB
            - JPEG / PNG / GIF / WEBP / HEIC / HEIF
            - 수정 시 `keepPhotoUrls`로 남길 기존 URL을 지정합니다. (아래 수정 API 참고)
            
            ## enum (영문 그대로 주고받음. 한글 라벨은 앱에서 매핑)
            - `bodyType`: SKINNY=마름, HEALTHY=적정, OVER_WEIGHT=과체중
            - `appetiteType`: LOW=식욕이 떨어짐, MIDDLE=식욕 평범, HIGH=식욕이 많음
            
            ## 권한
            - 남의 펫/없는 펫: `PET_404`
            - 없는 체중 기록: `PET_WEIGHT_404`
            """;

    public static final String CREATE_DESCRIPTION = """
            multipart/form-data: `data`(JSON, 필수) + `images`(0~3장).
            Access Token(JWT) 필수. 본인 소유 펫만. 미래 `recordedAt`은 `PET_WEIGHT_400_1`.
            
            `data` 필드:
            - `weight`: kg, 0.01 이상, 정수 3자리+소수 2자리 (예: 4.20)
            - `bodyType` / `appetiteType`: 위 enum
            - `memoContent`: 선택, 최대 1000자
            - `recordedAt`: `yyyy-MM-dd'T'HH:mm:ss`
            """;

    public static final String CREATE_DATA_EXAMPLE = """
            {
              "weight": 4.20,
              "bodyType": "HEALTHY",
              "appetiteType": "LOW",
              "memoContent": "식사 후 같은 시간대에 측정했어요.",
              "recordedAt": "2026-07-06T20:30:00"
            }
            """;

    public static final String CREATE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "PET_WEIGHT_CREATE_200",
              "message": "체중 기록이 저장되었습니다.",
              "result": {
                "petWeightId": 1,
                "petId": 1,
                "weight": 4.20,
                "bodyType": "HEALTHY",
                "appetiteType": "LOW",
                "memoContent": "식사 후 같은 시간대에 측정했어요.",
                "recordedAt": "2026-07-06T20:30:00",
                "photos": [
                  {"photoId": 1, "url": "https://cdn.example.com/pet-weight/1/a.jpg", "sortOrder": 0}
                ]
              }
            }
            """;

    public static final String UPDATE_DESCRIPTION = """
            multipart/form-data: `data`(JSON, 선택) + `images`(신규 추가, 선택). 코드 `PET_WEIGHT_UPDATE_200`.
            보낸 JSON 필드만 반영됩니다. 사진만 바꿀 때는 `data`를 생략해도 됩니다.
            
            ## keepPhotoUrls
            | data.keepPhotoUrls | 동작 |
            |---|---|
            | 생략(null) | 기존 사진 유지 + `images`만 추가 |
            | URL 목록 | 목록에 **없는** 기존 사진은 삭제 |
            | `[]` | 기존 사진 전부 삭제 후 `images`만 남김 |
            
            최종 장수(유지 + 신규) ≤ 3. 초과하면 `PET_WEIGHT_400_3`.
            `keepPhotoUrls`에는 상세 응답의 `photos[].url`을 그대로 넣으세요.
            """;

    public static final String UPDATE_DATA_EXAMPLE = """
            {
              "weight": 4.30,
              "memoContent": "산책 직후 측정",
              "keepPhotoUrls": ["https://cdn.example.com/pet-weight/1/a.jpg"]
            }
            """;

    public static final String UPDATE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "PET_WEIGHT_UPDATE_200",
              "message": "체중 기록이 수정되었습니다.",
              "result": {
                "petWeightId": 1,
                "petId": 1,
                "weight": 4.30,
                "bodyType": "HEALTHY",
                "appetiteType": "LOW",
                "memoContent": "산책 직후 측정",
                "recordedAt": "2026-07-06T20:30:00",
                "photos": [
                  {"photoId": 1, "url": "https://cdn.example.com/pet-weight/1/a.jpg", "sortOrder": 0},
                  {"photoId": 2, "url": "https://cdn.example.com/pet-weight/1/b.jpg", "sortOrder": 0}
                ]
              }
            }
            """;

    public static final String DELETE_DESCRIPTION = """
            체중 기록과 메모 사진을 삭제합니다. 코드 `PET_WEIGHT_DELETE_200`. `result`는 null.
            가장 최근 기록을 지우면 펫의 현재 체중이 남은 최신 기록으로 다시 맞춰집니다.
            """;

    public static final String DELETE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "PET_WEIGHT_DELETE_200",
              "message": "체중 기록이 삭제되었습니다.",
              "result": null
            }
            """;

    public static final String SUMMARY_DESCRIPTION = """
            건강요약 상단 카드입니다. 코드 `PET_WEIGHT_SUMMARY_200`.
            
            - `currentWeight`: 가장 최근 기록 체중. **기록이 하나도 없으면 펫 등록 시 체중**.
            - `lastRecordedAt`: 최근 기록 시각. 기록이 없으면 **null**.
            - `monthChange`: 이번 달 증감(kg, 소수 2자리).
              - 최근 기록이 이번 달 이전이면 **null** (이번 달 증감 없음)
              - 비교할 이전달 기록이 없고 이번 달 기록이 1건뿐이면 **null**
              - 양수=증가, 음수=감소
            """;

    public static final String SUMMARY_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "PET_WEIGHT_SUMMARY_200",
              "message": "체중 요약 조회에 성공했습니다.",
              "result": {
                "petId": 1,
                "currentWeight": 4.20,
                "lastRecordedAt": "2026-07-06T20:30:00",
                "monthChange": 0.10
              }
            }
            """;

    public static final String GRAPH_DESCRIPTION = """
            체중 변화 그래프 포인트입니다. 코드 `PET_WEIGHT_GRAPH_200`.
            
            | period | 구간 | 집계 |
            |---|---|---|
            | ONE_MONTH (기본) | 오늘 기준 최근 1개월 | **일** 단위. 같은 날 여러 건이면 **마지막 기록** |
            | SIX_MONTHS | 최근 6개월 | **월** 단위. `date`는 그 달 1일. 같은 달 마지막 기록 |
            
            - `points`는 날짜 오름차순. 기록 없는 날짜는 넣지 않습니다. (산책 일별 통계와 다름)
            - `minWeight` / `maxWeight`: 구간 내 최저/최고. 기록이 없으면 null.
            """;

    public static final String GRAPH_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "PET_WEIGHT_GRAPH_200",
              "message": "체중 그래프 조회에 성공했습니다.",
              "result": {
                "period": "ONE_MONTH",
                "startDate": "2026-06-07",
                "endDate": "2026-07-06",
                "minWeight": 3.90,
                "maxWeight": 4.20,
                "points": [
                  {"date": "2026-06-08", "weight": 3.90},
                  {"date": "2026-06-15", "weight": 3.95},
                  {"date": "2026-07-06", "weight": 4.20}
                ]
              }
            }
            """;

    public static final String LIST_DESCRIPTION = """
            월별 체중 기록 목록입니다. 최신 `recordedAt` 순. 코드 `PET_WEIGHT_LIST_200`.
            
            - `year`/`month` **둘 다 생략**하면 이번 달.
            - 한쪽만 보내도 이번 달로 조회합니다. (의료비 API와 다름)
            - 잘못된 month(1~12 아님)는 `PET_WEIGHT_400_2`.
            """;

    public static final String LIST_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "PET_WEIGHT_LIST_200",
              "message": "체중 기록 목록 조회에 성공했습니다.",
              "result": [
                {
                  "petWeightId": 2,
                  "petId": 1,
                  "weight": 4.20,
                  "bodyType": "HEALTHY",
                  "appetiteType": "MIDDLE",
                  "memoContent": "저녁 측정",
                  "recordedAt": "2026-07-20T21:00:00",
                  "photos": []
                },
                {
                  "petWeightId": 1,
                  "petId": 1,
                  "weight": 4.10,
                  "bodyType": "HEALTHY",
                  "appetiteType": "LOW",
                  "memoContent": "식사 후 같은 시간대에 측정했어요.",
                  "recordedAt": "2026-07-06T20:30:00",
                  "photos": [
                    {"photoId": 1, "url": "https://cdn.example.com/pet-weight/1/a.jpg", "sortOrder": 0}
                  ]
                }
              ]
            }
            """;

    public static final String GET_DESCRIPTION = "체중 기록 단건 + 사진 목록입니다. 코드 `PET_WEIGHT_GET_200`.";

    public static final String GET_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "PET_WEIGHT_GET_200",
              "message": "체중 기록 조회에 성공했습니다.",
              "result": {
                "petWeightId": 1,
                "petId": 1,
                "weight": 4.20,
                "bodyType": "HEALTHY",
                "appetiteType": "LOW",
                "memoContent": "식사 후 같은 시간대에 측정했어요.",
                "recordedAt": "2026-07-06T20:30:00",
                "photos": [
                  {"photoId": 1, "url": "https://cdn.example.com/pet-weight/1/a.jpg", "sortOrder": 0}
                ]
              }
            }
            """;

    public static final String PET_WEIGHT_400_1_EXAMPLE = """
            {"isSuccess":false,"code":"PET_WEIGHT_400_1","message":"미래 날짜로는 체중을 기록할 수 없습니다.","result":null}
            """;
    public static final String PET_WEIGHT_400_2_EXAMPLE = """
            {"isSuccess":false,"code":"PET_WEIGHT_400_2","message":"조회 기간이 올바르지 않습니다.","result":null}
            """;
    public static final String PET_WEIGHT_400_3_EXAMPLE = """
            {"isSuccess":false,"code":"PET_WEIGHT_400_3","message":"메모 사진은 최대 3장까지 첨부할 수 있습니다.","result":null}
            """;
    public static final String PET_WEIGHT_400_5_EXAMPLE = """
            {"isSuccess":false,"code":"PET_WEIGHT_400_5","message":"이미지 용량은 5MB 이하여야 합니다.","result":null}
            """;
    public static final String PET_WEIGHT_400_6_EXAMPLE = """
            {"isSuccess":false,"code":"PET_WEIGHT_400_6","message":"지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다.","result":null}
            """;
    public static final String PET_WEIGHT_404_EXAMPLE = """
            {"isSuccess":false,"code":"PET_WEIGHT_404","message":"체중 기록을 찾을 수 없습니다.","result":null}
            """;
    public static final String PET_404_EXAMPLE = """
            {"isSuccess":false,"code":"PET_404","message":"펫을 찾을 수 없습니다.","result":null}
            """;
}

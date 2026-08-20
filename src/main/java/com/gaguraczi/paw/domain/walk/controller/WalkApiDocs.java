package com.gaguraczi.paw.domain.walk.controller;

/**
 * Swagger annotation constants for {@link WalkController}. Documentation only.
 */
public final class WalkApiDocs {

    private WalkApiDocs() {
    }

    public static final String JWT_401_1_DESCRIPTION = "JWT_401_1. 토큰 만료 또는 미인증.";
    public static final String JWT_401_1_EXAMPLE = """
            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
            """;

    public static final String TAG_DESCRIPTION = """
            건강요약 - 산책 기록 API입니다. JWT Bearer 필수. 본인 펫만 조회/기록할 수 있습니다.
            
            ## 화면 흐름 (2가지)
            1. **수동 기록**: 산책이 끝난 뒤 `POST /api/walks` 로 시간·거리·날씨·강도를 한 번에 저장합니다.
            2. **자동 기록(타이머)**:
               - `POST /api/walks/start` → 타이머 시작 (DB 저장 없음, Redis 6시간)
               - 앱 재진입 시 `GET /api/walks/in-progress?petId=` 로 타이머 복구
               - `PATCH /api/walks/finish` 로 거리·날씨·강도를 채워 **완료 기록을 DB에 저장**
            
            ## 클라이언트 규칙
            - 날씨(`weatherType`)·강도(`walkType`)는 **한글 문자열**로 주고받습니다. enum 영문명(SUNNY, NORMAL 등)도 받지만 응답은 항상 한글입니다.
              - 날씨: `맑음` | `흐림` | `비` | `눈` | `바람`
              - 강도: `느긋` | `보통` | `활발`
            - `walkStatus`: `IN_PROGRESS`(타이머 중, walkId 없음) / `COMPLETED`(DB 저장 완료)
            - 목록·통계 API는 **완료 기록만** 포함합니다. 진행 중 타이머는 넣지 마세요.
            - 진행 중 조회에 실패(`WALK_404_3`)하면 타이머가 만료된 것이므로 새로 start 하세요.
            - 남의 펫/없는 펫은 `WALK_404_2` 입니다. (`PET_404`가 아닙니다)
            
            ## 단위
            - 거리 `walkingAmount`: km, 0.0 ~ 99.9
            - 시간 `durationMinutes`: 분. 서버가 startTime~endTime으로 계산합니다. 클라이언트가 보내지 않습니다.
            - 온도 `temp`: ℃
            """;

    public static final String CREATE_DESCRIPTION = """
            산책이 끝난 뒤 한 번에 저장합니다. HTTP 201, 코드 `WALK_201_1`.
            
            - 본인 펫만 가능. 미래 날짜/시각은 `WALK_400_5` / `WALK_400_1`.
            - `endTime`은 `startTime`보다 빠르면 안 됩니다.
            - **저장되는 산책 날짜는 `startTime`의 날짜**입니다. `walkDate`는 현재 필수 필드이지만 저장에 쓰이지 않으니 `startTime`과 같은 날짜를 보내세요.
            - `isUrine` / `isStool` 생략 시 false 로 저장됩니다.
            """;

    public static final String CREATE_REQ_EXAMPLE = """
            {
              "petId": 1,
              "walkDate": "2026-07-06",
              "weatherType": "맑음",
              "temp": 24,
              "startTime": "2026-07-06T18:20:00",
              "endTime": "2026-07-06T19:05:00",
              "walkingAmount": 1.8,
              "walkType": "보통",
              "isUrine": true,
              "isStool": true,
              "significant": "평소보다 힘들어 함"
            }
            """;

    public static final String CREATE_201_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_201_1",
              "message": "산책 기록이 저장되었습니다.",
              "result": {
                "walkId": 1,
                "petId": 1,
                "walkDate": "2026-07-06",
                "weatherType": "맑음",
                "temp": 24,
                "startTime": "2026-07-06T18:20:00",
                "endTime": "2026-07-06T19:05:00",
                "durationMinutes": 45,
                "walkingAmount": 1.8,
                "walkType": "보통",
                "isUrine": true,
                "isStool": true,
                "significant": "평소보다 힘들어 함",
                "walkStatus": "COMPLETED"
              }
            }
            """;

    public static final String START_DESCRIPTION = """
            타이머를 시작합니다. HTTP 201, 코드 `WALK_201_2`.
            
            - **walkId는 아직 없습니다.** 종료할 때까지 DB에 쓰지 않고 Redis에 **6시간** 보관합니다.
            - 그동안 `GET /api/walks/in-progress` 호출이 있으면 TTL이 다시 6시간으로 연장됩니다. 요청이 없으면 사라집니다.
            - `startTime` 생략 시 서버 현재 시각입니다.
            - 펫당 진행 중 산책은 1건입니다. 이미 있으면 `WALK_409_1`.
            - 종료 API(`PATCH /api/walks/finish`)에는 응답의 `petId`를 그대로 보내세요.
            """;

    public static final String START_201_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_201_2",
              "message": "산책을 시작했습니다.",
              "result": {
                "petId": 1,
                "walkDate": "2026-07-06",
                "startTime": "2026-07-06T18:20:00",
                "walkStatus": "IN_PROGRESS"
              }
            }
            """;

    public static final String FINISH_DESCRIPTION = """
            타이머를 종료하고 완료 기록을 DB에 저장합니다. 코드 `WALK_200_1`.
            
            - 식별자는 walkId가 아니라 **petId** 입니다.
            - Redis 세션이 없거나 6시간 만료면 `WALK_404_3`. 새로 start 해야 합니다.
            - `endTime` 생략 시 서버 현재 시각입니다.
            - 거리(`walkingAmount`)는 앱에서 측정한 km를 보냅니다.
            """;

    public static final String FINISH_REQ_EXAMPLE = """
            {
              "petId": 1,
              "endTime": "2026-07-06T19:05:00",
              "weatherType": "맑음",
              "temp": 24,
              "walkingAmount": 1.8,
              "walkType": "보통",
              "isUrine": true,
              "isStool": true,
              "significant": "평소보다 힘들어 함"
            }
            """;

    public static final String FINISH_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_1",
              "message": "산책을 종료했습니다.",
              "result": {
                "walkId": 1,
                "petId": 1,
                "walkDate": "2026-07-06",
                "weatherType": "맑음",
                "temp": 24,
                "startTime": "2026-07-06T18:20:00",
                "endTime": "2026-07-06T19:05:00",
                "durationMinutes": 45,
                "walkingAmount": 1.8,
                "walkType": "보통",
                "isUrine": true,
                "isStool": true,
                "significant": "평소보다 힘들어 함",
                "walkStatus": "COMPLETED"
              }
            }
            """;

    public static final String IN_PROGRESS_DESCRIPTION = """
            앱을 껐다 켰을 때 타이머를 복구합니다. 코드 `WALK_200_6`.
            
            ## 진행 중 응답에서 아직 없는 값
            - `walkId`, `weatherType`, `temp`, `endTime`, `durationMinutes`, `significant` → **null**
            - `walkingAmount` → `0`
            - `walkType` → `보통` (플레이스홀더)
            - `isUrine` / `isStool` → `false` (플레이스홀더)
            - `walkStatus` → `IN_PROGRESS`
            
            이 값으로 완료 화면을 채우지 마세요. 경과 시간은 클라이언트가 `startTime`과 현재 시각으로 계산합니다.
            세션이 없으면 `WALK_404_3` 입니다. (빈 객체가 아닙니다)
            """;

    public static final String IN_PROGRESS_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_6",
              "message": "진행 중인 산책을 조회했습니다.",
              "result": {
                "walkId": null,
                "petId": 1,
                "walkDate": "2026-07-06",
                "weatherType": null,
                "temp": null,
                "startTime": "2026-07-06T18:20:00",
                "endTime": null,
                "durationMinutes": null,
                "walkingAmount": 0,
                "walkType": "보통",
                "isUrine": false,
                "isStool": false,
                "significant": null,
                "walkStatus": "IN_PROGRESS"
              }
            }
            """;

    public static final String GET_DESCRIPTION = """
            완료된 산책 단건입니다. 진행 중 타이머는 walkId가 없어 이 API로 조회할 수 없습니다.
            코드 `WALK_200_2`. 없거나 남의 기록은 `WALK_404_1` / `WALK_403_1`.
            """;

    public static final String GET_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_2",
              "message": "산책 기록을 조회했습니다.",
              "result": {
                "walkId": 1,
                "petId": 1,
                "walkDate": "2026-07-06",
                "weatherType": "맑음",
                "temp": 24,
                "startTime": "2026-07-06T18:20:00",
                "endTime": "2026-07-06T19:05:00",
                "durationMinutes": 45,
                "walkingAmount": 1.8,
                "walkType": "보통",
                "isUrine": true,
                "isStool": true,
                "significant": "평소보다 힘들어 함",
                "walkStatus": "COMPLETED"
              }
            }
            """;

    public static final String LIST_DESCRIPTION = """
            완료된 산책 목록입니다. 최신 날짜·시작시간 내림차순. 코드 `WALK_200_3`.
            
            ## 조회 조건 (하나만 사용)
            | 파라미터 | 동작 |
            |---|---|
            | `date` | 그 날짜만 |
            | `startDate` + `endDate` | 기간 (둘 다 필수) |
            | 없음 | 해당 펫의 전체 |
            
            `startDate`/`endDate` 중 하나만 보내면 `WALK_400_2`.
            `date`가 있으면 기간 파라미터는 무시됩니다.
            """;

    public static final String LIST_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_3",
              "message": "산책 기록 목록을 조회했습니다.",
              "result": [
                {
                  "walkId": 2,
                  "walkDate": "2026-07-07",
                  "startTime": "2026-07-07T08:00:00",
                  "endTime": "2026-07-07T08:30:00",
                  "durationMinutes": 30,
                  "walkingAmount": 1.2,
                  "walkType": "활발",
                  "walkStatus": "COMPLETED"
                },
                {
                  "walkId": 1,
                  "walkDate": "2026-07-06",
                  "startTime": "2026-07-06T18:20:00",
                  "endTime": "2026-07-06T19:05:00",
                  "durationMinutes": 45,
                  "walkingAmount": 1.8,
                  "walkType": "보통",
                  "walkStatus": "COMPLETED"
                }
              ]
            }
            """;

    public static final String WEEKLY_DESCRIPTION = """
            대시보드 상단 카드용입니다. 코드 `WALK_200_7`.
            
            - 주는 **월요일~일요일** 입니다. `baseDate` 생략 시 오늘이 속한 주.
            - `averageMinutes` / `lastWeekAverageMinutes`: 1회당 평균(총 분 / 횟수, 정수 나눗셈). 기록이 없으면 0.
            - `diffMinutes`: 이번 주 평균 − 지난주 평균. **음수면 줄어든 것**입니다.
            - `walkCount` / `totalMinutes` / `totalDistance`: 이번 주 합계.
            """;

    public static final String WEEKLY_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_7",
              "message": "주간 산책 요약을 조회했습니다.",
              "result": {
                "weekStartDate": "2026-07-06",
                "weekEndDate": "2026-07-12",
                "averageMinutes": 45,
                "lastWeekAverageMinutes": 35,
                "diffMinutes": 10,
                "walkCount": 5,
                "totalMinutes": 225,
                "totalDistance": 9.4
              }
            }
            """;

    public static final String DAILY_DESCRIPTION = """
            '일별 산책 시간' 막대그래프용입니다. 코드 `WALK_200_8`.
            
            - 안 한 날도 `totalMinutes: 0`, `totalDistance: 0`, `walkCount: 0` 으로 **날짜를 채워** 내려줍니다.
            - 날짜 생략 시 **종료일 기준 최근 7일**(오늘 포함).
            - `dayOfWeek`는 그래프 라벨용 한글 한 글자입니다. (`월` `화` `수` `목` `금` `토` `일`)
            - 기간은 최대 366일. 초과하면 `WALK_400_7`.
            """;

    public static final String DAILY_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_8",
              "message": "일별 산책 통계를 조회했습니다.",
              "result": [
                {"walkDate": "2026-07-01", "dayOfWeek": "수", "totalMinutes": 0, "totalDistance": 0, "walkCount": 0},
                {"walkDate": "2026-07-02", "dayOfWeek": "목", "totalMinutes": 40, "totalDistance": 1.5, "walkCount": 1},
                {"walkDate": "2026-07-03", "dayOfWeek": "금", "totalMinutes": 90, "totalDistance": 3.2, "walkCount": 2}
              ]
            }
            """;

    public static final String UPDATE_DESCRIPTION = """
            보낸 필드만 반영합니다. 코드 `WALK_200_4`.
            
            - `weatherType` / `walkType` 도 한글 값입니다.
            - 시작·종료 시간을 바꾸면 `durationMinutes`는 서버가 다시 계산합니다.
            """;

    public static final String UPDATE_REQ_EXAMPLE = """
            {
              "walkType": "활발",
              "walkingAmount": 2.0,
              "significant": "다리를 살짝 절었음"
            }
            """;

    public static final String UPDATE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_4",
              "message": "산책 기록이 수정되었습니다.",
              "result": {
                "walkId": 1,
                "petId": 1,
                "walkDate": "2026-07-06",
                "weatherType": "맑음",
                "temp": 24,
                "startTime": "2026-07-06T18:20:00",
                "endTime": "2026-07-06T19:05:00",
                "durationMinutes": 45,
                "walkingAmount": 2.0,
                "walkType": "활발",
                "isUrine": true,
                "isStool": true,
                "significant": "다리를 살짝 절었음",
                "walkStatus": "COMPLETED"
              }
            }
            """;

    public static final String DELETE_DESCRIPTION = "완료된 산책 기록을 삭제합니다. 코드 `WALK_200_5`. 진행 중 타이머는 이 API로 취소할 수 없습니다.";

    public static final String DELETE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "WALK_200_5",
              "message": "산책 기록이 삭제되었습니다.",
              "result": { "walkId": 1 }
            }
            """;

    public static final String WALK_400_1_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_400_1","message":"종료 시간은 시작 시간보다 빠를 수 없습니다.","result":null}
            """;
    public static final String WALK_400_2_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_400_2","message":"조회 시작일이 종료일보다 늦을 수 없습니다.","result":null}
            """;
    public static final String WALK_400_5_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_400_5","message":"미래 날짜의 산책은 기록할 수 없습니다.","result":null}
            """;
    public static final String WALK_400_6_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_400_6","message":"날씨는 맑음, 흐림, 비, 눈, 바람 중 하나여야 합니다.","result":null}
            """;
    public static final String WALK_400_7_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_400_7","message":"통계 조회 기간은 최대 366일입니다.","result":null}
            """;
    public static final String WALK_403_1_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_403_1","message":"해당 산책 기록에 접근할 권한이 없습니다.","result":null}
            """;
    public static final String WALK_404_1_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_404_1","message":"존재하지 않는 산책 기록입니다.","result":null}
            """;
    public static final String WALK_404_2_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_404_2","message":"존재하지 않는 반려동물입니다.","result":null}
            """;
    public static final String WALK_404_3_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_404_3","message":"진행 중인 산책이 없습니다.","result":null}
            """;
    public static final String WALK_409_1_EXAMPLE = """
            {"isSuccess":false,"code":"WALK_409_1","message":"이미 진행 중인 산책이 있습니다.","result":null}
            """;
}

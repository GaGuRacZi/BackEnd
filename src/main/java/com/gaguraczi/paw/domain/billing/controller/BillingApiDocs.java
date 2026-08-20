package com.gaguraczi.paw.domain.billing.controller;

/**
 * Swagger annotation constants for billing/subscription APIs. Documentation only.
 */
public final class BillingApiDocs {

    private BillingApiDocs() {
    }

    public static final String JWT_401_1_DESCRIPTION = "JWT 만료/미인증 (JWT_401_1)";
    public static final String JWT_401_1_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "JWT_401_1",
              "message": "token 유효기간이 만료되었습니다.",
              "result": null
            }
            """;

    public static final String JWT_403_2_DESCRIPTION = "유효하지 않은 token (JWT_403_2)";
    public static final String JWT_403_2_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "JWT_403_2",
              "message": "유효하지 않은 token입니다.",
              "result": null
            }
            """;

    public static final String JWT_403_3_DESCRIPTION = "ADMIN 권한 없음 (JWT_403_3)";
    public static final String JWT_403_3_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "JWT_403_3",
              "message": "권한 정보가 없는 token입니다.",
              "result": null
            }
            """;

    public static final String COMMON_400_PLAN_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "COMMON_400",
              "message": "잘못된 요청입니다.",
              "result": {
                "plan": "요금제는 필수입니다."
              }
            }
            """;

    public static final String BILLING_400_1_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "BILLING_400_1",
              "message": "이미 이용 중인 요금제입니다.",
              "result": null
            }
            """;

    public static final String BILLING_404_1_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "BILLING_404_1",
              "message": "사용자를 찾을 수 없습니다.",
              "result": null
            }
            """;

    public static final String BILLING_404_2_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "BILLING_404_2",
              "message": "결제 내역을 찾을 수 없습니다.",
              "result": null
            }
            """;

    public static final String MYPAGE_400_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "MYPAGE_400",
              "message": "요청 처리에 실패했습니다.",
              "result": null
            }
            """;

    public static final String TAG_DESCRIPTION = """
            마이페이지 요금제/결제 API입니다. JWT Bearer 필수. PG 연동 없이 결제는 서버에서 즉시 성공 처리합니다.
            
            ## 요금제
            | 플랜 | 표시명 | 가격 | 코인 |
            |---|---|---|---|
            | BASIC | 꼬마 젤리 | 0원 | 가입 시 3개 (월 지급 없음) |
            | PRO | 새싹 젤리 | 4,900원 | 매월 10개 지급 |
            | ULTIMATE | 어른 젤리 | 9,900원 | 무제한 (AI 요약 차감 없음) |
            
            ## 변경 규칙
            - 업그레이드: 즉시 모의 결제 후 플랜 적용. 기간은 지금부터 1개월. 결제 내역(`PURCHASE`)이 생깁니다.
            - 다운그레이드/해지: 현재 플랜은 `periodEnd`까지 유지되고 `pendingPlan`만 저장됩니다. `status`는 `PENDING_CHANGE`.
            - 같은 플랜을 다시 요청하면 예약된 다운그레이드를 취소합니다. 예약이 없으면 `BILLING_400_1`.
            - 일할 정산 없음. 유료 전환·갱신은 정가 전액으로 결제 내역만 남깁니다.
            - 다운그레이드해도 이미 지급된 코인은 회수하지 않습니다.
            - 매일 00:10 KST에 기간이 끝난 구독을 갱신하거나 `pendingPlan`을 적용합니다.
            
            ## 클라이언트 규칙
            - `nextCursor`는 opaque 값입니다. 해석·변조하지 말고 다음 요청에 그대로 전달하세요.
            - BASIC은 `periodStart`/`periodEnd`가 null입니다.
            """;

    public static final String GET_DESCRIPTION = """
            Access Token(JWT) 필수. 현재 요금제, 남은 코인, 다음 결제일, 예약된 변경, 전체 플랜 목록을 반환합니다.
            
            - 구독 행이 없으면 BASIC으로 생성합니다.
            - 다운그레이드가 예약돼 있으면 `pendingPlan`과 `status=PENDING_CHANGE`가 채워집니다.
            - `plans`는 화면 요금제 카드용 카탈로그입니다.
            """;

    public static final String CHANGE_DESCRIPTION = """
            Access Token(JWT) 필수. `{ "plan": "PRO" }` 형태로 요금제를 변경하거나 예약합니다.
            
            - **업그레이드**(BASIC→PRO/ULTIMATE, PRO→ULTIMATE): 즉시 모의 결제 후 적용. 코드 `BILLING_PLAN_CHANGE_200`.
            - **다운그레이드**(ULTIMATE→PRO/BASIC, PRO→BASIC): `periodEnd`가 남아 있으면 예약만 하고 현재 플랜은 유지합니다.
            - **예약 취소**: 현재와 같은 `plan`을 다시 보내면 `pendingPlan`을 지웁니다. 예약이 없으면 `BILLING_400_1`.
            - `plan` 누락/잘못된 enum은 `COMMON_400`.
            """;

    public static final String PAYMENT_LIST_DESCRIPTION = """
            Access Token(JWT) 필수. 본인 결제 내역을 최신순 커서로 조회합니다.
            
            - size 기본 20, 최대 50
            - `cursor`는 이전 응답 `nextCursor`를 그대로 전달. 잘못된 값이면 `MYPAGE_400`
            - `type`: `PURCHASE`(요금제 변경) / `RENEWAL`(월 갱신)
            - 관리자 강제 변경은 결제 내역을 만들지 않습니다.
            """;

    public static final String PAYMENT_DETAIL_DESCRIPTION = """
            Access Token(JWT) 필수. 본인 결제 건만 조회할 수 있습니다.
            없거나 다른 사람 결제 ID면 `BILLING_404_2`입니다.
            """;

    public static final String FORCE_DESCRIPTION = """
            ADMIN JWT 필수. 대상 uid의 요금제를 **즉시** 적용합니다.
            
            - 다음 결제일 대기(`pendingPlan`)를 건너뛰고 지웁니다.
            - 결제 내역은 남기지 않습니다.
            - BASIC이면 `periodStart`/`periodEnd`는 null입니다.
            - 없는 uid는 `BILLING_404_1`.
            """;

    public static final String ADMIN_BILLING_TAG_DESCRIPTION = """
            관리자 요금제 API입니다. ADMIN 역할 JWT 필수.
            
            `POST /admin/subscriptions/force`는 즉시 적용되며 결제 내역을 남기지 않습니다.
            유저 화면의 변경/예약 규칙과 다릅니다.
            """;

    public static final String CHANGE_REQ_EXAMPLE = """
            {
              "plan": "PRO"
            }
            """;

    public static final String DOWNGRADE_REQ_EXAMPLE = """
            {
              "plan": "BASIC"
            }
            """;

    public static final String FORCE_REQ_EXAMPLE = """
            {
              "uid": "550e8400-e29b-41d4-a716-446655440000",
              "plan": "BASIC"
            }
            """;

    public static final String PLAN_CATALOG_JSON = """
                    [
                      {
                        "plan": "BASIC",
                        "displayName": "꼬마 젤리",
                        "priceWon": 0,
                        "monthlyCoinGrant": null,
                        "includedCoins": 3,
                        "unlimitedCoin": false
                      },
                      {
                        "plan": "PRO",
                        "displayName": "새싹 젤리",
                        "priceWon": 4900,
                        "monthlyCoinGrant": 10,
                        "includedCoins": 10,
                        "unlimitedCoin": false
                      },
                      {
                        "plan": "ULTIMATE",
                        "displayName": "어른 젤리",
                        "priceWon": 9900,
                        "monthlyCoinGrant": null,
                        "includedCoins": null,
                        "unlimitedCoin": true
                      }
                    ]""";

    public static final String SUBSCRIPTION_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "BILLING_PLAN_200",
              "message": "요금제 조회에 성공했습니다.",
              "result": {
                "plan": "PRO",
                "displayName": "새싹 젤리",
                "priceWon": 4900,
                "monthlyCoinGrant": 10,
                "unlimitedCoin": false,
                "coin": 13,
                "periodStart": "2026-08-20T23:10:00",
                "periodEnd": "2026-09-20T23:10:00",
                "pendingPlan": null,
                "pendingDisplayName": null,
                "status": "ACTIVE",
                "plans":
            """ + PLAN_CATALOG_JSON + """
              }
            }
            """;

    public static final String SUBSCRIPTION_PENDING_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "BILLING_PLAN_200",
              "message": "요금제 조회에 성공했습니다.",
              "result": {
                "plan": "PRO",
                "displayName": "새싹 젤리",
                "priceWon": 4900,
                "monthlyCoinGrant": 10,
                "unlimitedCoin": false,
                "coin": 13,
                "periodStart": "2026-08-20T23:10:00",
                "periodEnd": "2026-09-20T23:10:00",
                "pendingPlan": "BASIC",
                "pendingDisplayName": "꼬마 젤리",
                "status": "PENDING_CHANGE",
                "plans":
            """ + PLAN_CATALOG_JSON + """
              }
            }
            """;

    public static final String SUBSCRIPTION_CHANGE_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "BILLING_PLAN_CHANGE_200",
              "message": "요금제가 변경되었습니다.",
              "result": {
                "plan": "ULTIMATE",
                "displayName": "어른 젤리",
                "priceWon": 9900,
                "monthlyCoinGrant": null,
                "unlimitedCoin": true,
                "coin": 13,
                "periodStart": "2026-08-20T23:10:00",
                "periodEnd": "2026-09-20T23:10:00",
                "pendingPlan": null,
                "pendingDisplayName": null,
                "status": "ACTIVE",
                "plans":
            """ + PLAN_CATALOG_JSON + """
              }
            }
            """;

    public static final String SUBSCRIPTION_DOWNGRADE_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "BILLING_PLAN_CHANGE_200",
              "message": "요금제가 변경되었습니다.",
              "result": {
                "plan": "PRO",
                "displayName": "새싹 젤리",
                "priceWon": 4900,
                "monthlyCoinGrant": 10,
                "unlimitedCoin": false,
                "coin": 13,
                "periodStart": "2026-08-20T23:10:00",
                "periodEnd": "2026-09-20T23:10:00",
                "pendingPlan": "BASIC",
                "pendingDisplayName": "꼬마 젤리",
                "status": "PENDING_CHANGE",
                "plans":
            """ + PLAN_CATALOG_JSON + """
              }
            }
            """;

    public static final String SUBSCRIPTION_FORCE_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "BILLING_PLAN_FORCE_200",
              "message": "요금제가 강제 변경되었습니다.",
              "result": {
                "plan": "BASIC",
                "displayName": "꼬마 젤리",
                "priceWon": 0,
                "monthlyCoinGrant": null,
                "unlimitedCoin": false,
                "coin": 13,
                "periodStart": null,
                "periodEnd": null,
                "pendingPlan": null,
                "pendingDisplayName": null,
                "status": "ACTIVE",
                "plans":
            """ + PLAN_CATALOG_JSON + """
              }
            }
            """;

    public static final String PAYMENT_LIST_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "BILLING_PAYMENT_LIST_200",
              "message": "결제 내역 조회에 성공했습니다.",
              "result": {
                "content": [
                  {
                    "paymentId": 2,
                    "plan": "ULTIMATE",
                    "displayName": "어른 젤리",
                    "amount": 9900,
                    "type": "PURCHASE",
                    "status": "SUCCESS",
                    "paidAt": "2026-08-20T23:10:00"
                  },
                  {
                    "paymentId": 1,
                    "plan": "PRO",
                    "displayName": "새싹 젤리",
                    "amount": 4900,
                    "type": "RENEWAL",
                    "status": "SUCCESS",
                    "paidAt": "2026-07-20T23:10:00"
                  }
                ],
                "nextCursor": "MjAyNi0wNy0yMFQyMzoxMDowMHwx",
                "hasNext": true,
                "size": 20
              }
            }
            """;

    public static final String PAYMENT_ITEM_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "BILLING_PAYMENT_DETAIL_200",
              "message": "결제 상세 조회에 성공했습니다.",
              "result": {
                "paymentId": 1,
                "plan": "PRO",
                "displayName": "새싹 젤리",
                "amount": 4900,
                "type": "PURCHASE",
                "status": "SUCCESS",
                "paidAt": "2026-08-20T23:10:00"
              }
            }
            """;
}

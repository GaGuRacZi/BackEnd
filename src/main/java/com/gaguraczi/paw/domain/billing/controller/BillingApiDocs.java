package com.gaguraczi.paw.domain.billing.controller;

/**
 * Swagger annotation constants for billing/subscription APIs. Documentation only.
 */
public final class BillingApiDocs {

    private BillingApiDocs() {
    }

    public static final String JWT_401_1_DESCRIPTION = "JWT_401_1. 토큰 만료 또는 미인증.";
    public static final String JWT_401_1_EXAMPLE = """
            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
            """;

    public static final String JWT_403_3_DESCRIPTION = "JWT_403_3. ADMIN 권한 없음.";
    public static final String JWT_403_3_EXAMPLE = """
            {"isSuccess":false,"code":"JWT_403_3","message":"권한 정보가 없는 token입니다.","result":null}
            """;

    public static final String TAG_DESCRIPTION = """
            마이페이지 요금제/결제 API입니다. JWT Bearer 필수. PG 연동 없이 결제는 서버에서 즉시 성공 처리합니다.
            
            ## 요금제
            | 플랜 | 표시명 | 가격 | 코인 |
            | BASIC | 꼬마 젤리 | 0원 | 가입 시 3개 (월 지급 없음) |
            | PRO | 새싹 젤리 | 4,900원 | 매월 10개 지급 |
            | ULTIMATE | 어른 젤리 | 9,900원 | 무제한 (AI 요약 차감 없음) |
            
            ## 변경 규칙
            - 업그레이드: 즉시 모의 결제 후 플랜 적용. 기간은 지금부터 1개월.
            - 다운그레이드/해지: 현재 플랜은 `periodEnd`까지 유지되고 `pendingPlan`만 저장됩니다.
            - 같은 플랜을 다시 요청하면 예약된 다운그레이드를 취소합니다. 예약이 없으면 `BILLING_400_1`.
            - 일할 정산 없음. 유료 전환·갱신은 정가 전액으로 결제 내역만 남깁니다.
            """;

    public static final String GET_DESCRIPTION = """
            현재 요금제, 남은 코인, 다음 결제일, 예약된 변경, 전체 플랜 목록을 반환합니다.
            구독 행이 없으면 BASIC으로 생성합니다.
            """;

    public static final String CHANGE_DESCRIPTION = """
            `{ "plan": "PRO" }` 형태로 요금제를 변경하거나 예약합니다.
            업그레이드는 즉시 성공하고 결제 내역이 생깁니다. 다운그레이드는 다음 결제일까지 대기합니다.
            """;

    public static final String PAYMENT_LIST_DESCRIPTION = """
            본인 결제 내역을 최신순 커서로 조회합니다. size 기본 20, 최대 50.
            `nextCursor`는 opaque 값이며 다음 요청에 그대로 전달하세요.
            """;

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
                "plans": [
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
                ]
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

    public static final String FORCE_REQ_EXAMPLE = """
            {
              "uid": "550e8400-e29b-41d4-a716-446655440000",
              "plan": "BASIC"
            }
            """;
}

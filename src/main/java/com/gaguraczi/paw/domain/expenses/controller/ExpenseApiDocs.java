package com.gaguraczi.paw.domain.expenses.controller;

/**
 * Swagger annotation constants for {@link ExpenseController}. Documentation only.
 */
public final class ExpenseApiDocs {

    private ExpenseApiDocs() {
    }

    public static final String JWT_401_1_DESCRIPTION = "JWT_401_1. 토큰 만료 또는 미인증.";
    public static final String JWT_401_1_EXAMPLE = """
            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
            """;

    public static final String TAG_DESCRIPTION = """
            건강요약 - 의료비 API입니다. JWT Bearer 필수. 본인 펫만 기록/조회할 수 있습니다.
            
            ## 화면 구성
            1. 상단 카드: `GET /api/v1/pets/{petId}/expenses/summary` → 이번 달 병원비 + 누적 총액
            2. 월별 내역: `GET /api/v1/pets/{petId}/expenses?year=&month=` → 목록 + 그 달 합계
            3. 작성/수정: `POST /api/v1/pets/{petId}/expenses`, `PUT /api/v1/expenses/{expenseId}`
            4. 상세: `GET /api/v1/expenses/{expenseId}` (세부 항목 포함)
            
            ## 금액 규칙
            - `expenseAmount`(결제 금액)와 `expenseDetails[].expenseAmount` 합계가 **같아야** 등록됩니다. 다르면 `EXPENSE_400_4`.
            - 세부 항목은 **최소 1개**.
            - 금액 단위는 원(정수). 미래 날짜는 `EXPENSE_400_3`.
            
            ## 결제수단 (`paymentType`)
            요청은 enum 영문명, 응답에는 표시용 `paymentTypeLabel`도 같이 옵니다.
            | paymentType | paymentTypeLabel |
            |---|---|
            | CARD | 카드 |
            | TRANSFER | 계좌이체 |
            | VIRTUAL_ACCOUNT | 가상계좌 |
            | MOBILE | 휴대폰 결제 |
            | EASY_PAY | 간편결제 |
            
            ## 권한
            - 남의 펫/없는 펫: `PET_404`
            - 없는 의료비: `EXPENSE_404`
            - 남의 의료비: `EXPENSE_403`
            
            ## 연월 파라미터
            `year`와 `month`는 **둘 다 생략(이번 달)** 하거나 **둘 다 지정**하세요. 한쪽만 보내면 `EXPENSE_400_2`.
            `month`는 1~12.
            """;

    public static final String CREATE_DESCRIPTION = """
            병원비 한 건과 세부 항목을 함께 저장합니다. 코드 `EXPENSE_CREATE_200`.
            
            - `expenseDate`: `yyyy-MM-dd`. 오늘 이후는 불가.
            - `expenseDetails`는 어디에 얼마를 썼는지 항목 목록입니다. 합계 = `expenseAmount`.
            - 병원명(`expenseName`)은 trim 되어 저장됩니다.
            """;

    public static final String CREATE_REQ_EXAMPLE = """
            {
              "expenseAmount": 77000,
              "expenseDate": "2026-07-06",
              "paymentType": "EASY_PAY",
              "expenseName": "행복동물병원",
              "expenseDetails": [
                { "expenseDetailName": "유선종양 수술", "expenseAmount": 42000 },
                { "expenseDetailName": "혈액검사", "expenseAmount": 35000 }
              ]
            }
            """;

    public static final String DETAIL_RESULT_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "EXPENSE_CREATE_200",
              "message": "의료비 기록이 저장되었습니다.",
              "result": {
                "expenseId": 1,
                "petId": 1,
                "expenseAmount": 77000,
                "expenseDate": "2026-07-06",
                "paymentType": "EASY_PAY",
                "paymentTypeLabel": "간편결제",
                "expenseName": "행복동물병원",
                "expenseDetails": [
                  { "expenseDetailId": 1, "expenseDetailName": "유선종양 수술", "expenseAmount": 42000 },
                  { "expenseDetailId": 2, "expenseDetailName": "혈액검사", "expenseAmount": 35000 }
                ]
              }
            }
            """;

    public static final String UPDATE_DESCRIPTION = """
            작성자 본인만 가능. 보낸 필드만 반영합니다. 코드 `EXPENSE_UPDATE_200`.
            
            - `expenseDetails`를 **보내면** 기존 세부 항목을 이 목록으로 **통째로 교체**합니다. (부분 수정 아님)
            - `expenseDetails`를 **생략**하면 기존 세부 항목은 그대로입니다.
            - 빈 배열 `[]` 은 `EXPENSE_400_1`.
            - 날짜를 바꾸면 미래 날짜인지 다시 검사합니다.
            """;

    public static final String UPDATE_REQ_EXAMPLE = """
            {
              "expenseAmount": 80000,
              "expenseDetails": [
                { "expenseDetailName": "유선종양 수술", "expenseAmount": 45000 },
                { "expenseDetailName": "혈액검사", "expenseAmount": 35000 }
              ]
            }
            """;

    public static final String UPDATE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "EXPENSE_UPDATE_200",
              "message": "의료비 기록이 수정되었습니다.",
              "result": {
                "expenseId": 1,
                "petId": 1,
                "expenseAmount": 80000,
                "expenseDate": "2026-07-06",
                "paymentType": "EASY_PAY",
                "paymentTypeLabel": "간편결제",
                "expenseName": "행복동물병원",
                "expenseDetails": [
                  { "expenseDetailId": 3, "expenseDetailName": "유선종양 수술", "expenseAmount": 45000 },
                  { "expenseDetailId": 4, "expenseDetailName": "혈액검사", "expenseAmount": 35000 }
                ]
              }
            }
            """;

    public static final String DELETE_DESCRIPTION = """
            작성자 본인만 삭제합니다. 코드 `EXPENSE_DELETE_200`. `result`는 null 입니다.
            """;

    public static final String DELETE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "EXPENSE_DELETE_200",
              "message": "의료비 기록이 삭제되었습니다.",
              "result": null
            }
            """;

    public static final String LIST_DESCRIPTION = """
            지정한 연월의 내역을 **최신 이용일 순**으로 조회합니다. 코드 `EXPENSE_LIST_200`.
            
            - `year`/`month` 생략 시 이번 달.
            - `monthlyTotalAmount`는 그 달 `expenseAmount` 합계입니다. 화면 월 합계에 그대로 쓰세요.
            - 목록 항목에는 세부 항목이 없습니다. 상세는 `GET /api/v1/expenses/{expenseId}`.
            """;

    public static final String LIST_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "EXPENSE_LIST_200",
              "message": "월별 의료비 내역을 조회했습니다.",
              "result": {
                "year": 2026,
                "month": 7,
                "monthlyTotalAmount": 149000,
                "expenses": [
                  { "expenseId": 2, "expenseName": "행복동물병원", "expenseDate": "2026-07-20", "expenseAmount": 72000 },
                  { "expenseId": 1, "expenseName": "행복동물병원", "expenseDate": "2026-07-06", "expenseAmount": 77000 }
                ]
              }
            }
            """;

    public static final String SUMMARY_DESCRIPTION = """
            건강요약 상단 카드용입니다. 코드 `EXPENSE_SUMMARY_200`.
            
            - `monthlyTotalAmount`: 지정 연월(기본 이번 달) 병원비 합계. 없으면 0.
            - `totalAmount`: 해당 펫의 **누적 총 병원비**. 연월과 무관합니다.
            """;

    public static final String SUMMARY_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "EXPENSE_SUMMARY_200",
              "message": "의료비 요약을 조회했습니다.",
              "result": {
                "year": 2026,
                "month": 7,
                "monthlyTotalAmount": 149000,
                "totalAmount": 980000
              }
            }
            """;

    public static final String DETAIL_DESCRIPTION = """
            단건 + 세부 항목 목록입니다. 코드 `EXPENSE_DETAIL_200`.
            UI에는 `paymentTypeLabel`(한글)을 보여주고, 수정 폼에는 `paymentType`을 그대로 넣으세요.
            """;

    public static final String DETAIL_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "EXPENSE_DETAIL_200",
              "message": "의료비 상세 내역을 조회했습니다.",
              "result": {
                "expenseId": 1,
                "petId": 1,
                "expenseAmount": 77000,
                "expenseDate": "2026-07-06",
                "paymentType": "EASY_PAY",
                "paymentTypeLabel": "간편결제",
                "expenseName": "행복동물병원",
                "expenseDetails": [
                  { "expenseDetailId": 1, "expenseDetailName": "유선종양 수술", "expenseAmount": 42000 },
                  { "expenseDetailId": 2, "expenseDetailName": "혈액검사", "expenseAmount": 35000 }
                ]
              }
            }
            """;

    public static final String EXPENSE_400_1_EXAMPLE = """
            {"isSuccess":false,"code":"EXPENSE_400_1","message":"세부 항목은 최소 1개 이상이어야 합니다.","result":null}
            """;
    public static final String EXPENSE_400_2_EXAMPLE = """
            {"isSuccess":false,"code":"EXPENSE_400_2","message":"조회 연월이 올바르지 않습니다.","result":null}
            """;
    public static final String EXPENSE_400_3_EXAMPLE = """
            {"isSuccess":false,"code":"EXPENSE_400_3","message":"미래 날짜로는 의료비를 기록할 수 없습니다.","result":null}
            """;
    public static final String EXPENSE_400_4_EXAMPLE = """
            {"isSuccess":false,"code":"EXPENSE_400_4","message":"결제 금액과 세부 항목 금액 합계가 일치하지 않습니다.","result":null}
            """;
    public static final String EXPENSE_403_EXAMPLE = """
            {"isSuccess":false,"code":"EXPENSE_403","message":"권한이 없습니다.","result":null}
            """;
    public static final String EXPENSE_404_EXAMPLE = """
            {"isSuccess":false,"code":"EXPENSE_404","message":"의료비 기록을 찾을 수 없습니다.","result":null}
            """;
    public static final String PET_404_EXAMPLE = """
            {"isSuccess":false,"code":"PET_404","message":"펫을 찾을 수 없습니다.","result":null}
            """;
}

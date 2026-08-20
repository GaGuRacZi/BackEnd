package com.gaguraczi.paw.domain.mypage.controller;

/**
 * Swagger annotation constants for admin inquiry APIs. Documentation only.
 */
public final class AdminInquiryApiDocs {

    private AdminInquiryApiDocs() {
    }

    public static final String TAG_DESCRIPTION = """
            관리자 문의 API입니다. ADMIN 역할 JWT 필수.
            
            - 목록은 전체 유저 문의를 최신순 커서로 조회합니다.
            - `status`, `inquiryType`은 선택 필터입니다. 둘 다 생략하면 전체.
            - 답변(`PATCH`)은 `answer`를 저장하고 `status`를 `ANSWERED`로 바꿉니다. 재답변은 덮어씁니다.
            - `nextCursor`는 opaque 값이며 다음 요청에 그대로 전달하세요.
            """;

    public static final String LIST_DESCRIPTION = """
            ADMIN JWT 필수. 전체 유저 문의를 최신순 커서로 조회합니다.
            
            - size 기본 20, 최대 50
            - `cursor`는 이전 응답 `nextCursor`를 그대로 전달. 잘못된 값이면 `MYPAGE_400`
            - `status`: RECEIVED | IN_PROGRESS | ANSWERED
            - `inquiryType`: ACCOUNT | PAYMENT | PET | COMMUNITY | ETC
            """;

    public static final String DETAIL_DESCRIPTION = """
            ADMIN JWT 필수. 문의 ID로 상세를 조회합니다. 작성자 uid·닉네임·이메일을 포함합니다.
            없으면 `MYPAGE_404_1`.
            """;

    public static final String ANSWER_DESCRIPTION = """
            ADMIN JWT 필수. 답변을 저장하고 `status`를 `ANSWERED`로 바꿉니다.
            
            - 재답변은 기존 `answer`를 덮어씁니다.
            - `answer` 공백/누락은 `COMMON_400`.
            - 없는 문의는 `MYPAGE_404_1`.
            """;

    public static final String ANSWER_REQ_EXAMPLE = """
            {
              "answer": "결제 내역을 확인했습니다. 재시도해 주세요."
            }
            """;

    public static final String COMMON_400_ANSWER_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "COMMON_400",
              "message": "잘못된 요청입니다.",
              "result": {
                "answer": "답변 내용은 필수입니다."
              }
            }
            """;

    public static final String MYPAGE_404_1_EXAMPLE = """
            {
              "isSuccess": false,
              "code": "MYPAGE_404_1",
              "message": "문의 내역을 찾을 수 없습니다.",
              "result": null
            }
            """;

    public static final String LIST_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "ADMIN_INQUIRY_LIST_200",
              "message": "문의 목록 조회에 성공했습니다.",
              "result": {
                "content": [
                  {
                    "inquiryId": 1,
                    "uid": "550e8400-e29b-41d4-a716-446655440000",
                    "nickname": "길동이",
                    "email": "user@example.com",
                    "inquiryType": "PAYMENT",
                    "content": "구독 결제가 반복해서 실패해요.",
                    "attachmentUrls": [
                      "https://cdn.example.com/inquiry/uuid.png"
                    ],
                    "status": "RECEIVED",
                    "answer": null,
                    "createdAt": "2026-08-20T11:00:00"
                  }
                ],
                "nextCursor": null,
                "hasNext": false,
                "size": 20
              }
            }
            """;

    public static final String DETAIL_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "ADMIN_INQUIRY_DETAIL_200",
              "message": "문의 상세 조회에 성공했습니다.",
              "result": {
                "inquiryId": 1,
                "uid": "550e8400-e29b-41d4-a716-446655440000",
                "nickname": "길동이",
                "email": "user@example.com",
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
            """;

    public static final String ANSWER_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "ADMIN_INQUIRY_ANSWER_200",
              "message": "문의 답변이 등록되었습니다.",
              "result": {
                "inquiryId": 1,
                "uid": "550e8400-e29b-41d4-a716-446655440000",
                "nickname": "길동이",
                "email": "user@example.com",
                "inquiryType": "PAYMENT",
                "content": "구독 결제가 반복해서 실패해요.",
                "attachmentUrls": [
                  "https://cdn.example.com/inquiry/uuid.png"
                ],
                "status": "ANSWERED",
                "answer": "결제 내역을 확인했습니다. 재시도해 주세요.",
                "createdAt": "2026-08-20T11:00:00"
              }
            }
            """;
}

package com.gaguraczi.paw.domain.notification.controller;

/**
 * Swagger annotation constants for {@link NotificationController}. Documentation only.
 */
public final class NotificationApiDocs {

    private NotificationApiDocs() {
    }

    public static final String JWT_401_1_DESCRIPTION = "JWT_401_1. 토큰 만료 또는 미인증.";
    public static final String JWT_401_1_EXAMPLE = """
            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
            """;

    public static final String TAG_DESCRIPTION = """
            알림 인박스 API입니다. JWT Bearer 필수 (`Authorization: Bearer {accessToken}`).
            오늘/어제 그룹은 앱이 `createdAt`(KST)으로 나눕니다.
            
            ## 관련 설정
            - 채널 on/off·방해 금지: `PATCH /mypage/notifications/settings`
            - FCM 토큰: `PUT /users/me/push-token`
            
            ## 탭 이동
            `ctaLabel` + `targetType` + `targetId`로 화면을 엽니다.
            
            | category | 설정 필드 (기본) | targetType | targetId | FCM type |
            |---|---|---|---|---|
            | TODO | todoAlarm (ON) | TODO | todoId | TODO_REMINDER |
            | AI | aiAnalysisAlarm (ON) | VISIT | visitId | VISIT_READY / VISIT_FAILED |
            | COMMUNITY | communityAlarm (ON) | POST | postId | COMMUNITY_COMMENT |
            | CHAT | chatAlarm (**OFF**) | CHAT_ROOM | roomId | CHAT_MESSAGE |
            | EMERGENCY | 항상 허용 | MAP 등 | 상황별 | 건강 이상. DND 예외 가능 |
            
            ## 채팅 알림 (CHAT)
            `POST /chat/rooms/{roomId}/messages` 가 커밋된 뒤 상대방에게만 쌓입니다.
            - 제목: `{닉네임}님의 메시지` (없으면 `새 메시지`)
            - 본문: 텍스트 미리보기(최대 100자) 또는 `사진을 보냈습니다`
            - `ctaLabel`: `채팅 보기` → `GET /chat/rooms/{targetId}`
            - FCM data(문자열): `type=CHAT_MESSAGE`, `category=CHAT`, `roomId`, `postId`, `senderId`, `messageId`
            - `chatAlarm`이 false면 인박스·FCM 모두 없습니다. 방해 금지 중이면 인박스만 남습니다.
            """;

    public static final String LIST_DESCRIPTION = """
            내 알림을 최신순으로 줍니다. 코드 `NOTI_LIST_200`.
            
            - `category` 생략 시 전체. `TODO` | `AI` | `COMMUNITY` | `CHAT` | `EMERGENCY`
            - size 기본 20, 최대 50.
            - `cursor`는 이전 응답 `nextCursor`를 그대로 전달합니다. 변조하면 `NOTI_400`.
            - CHAT 아이템: `targetType=CHAT_ROOM`, `targetId=roomId`. 채팅방으로 이동하세요.
            """;

    public static final String LIST_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "NOTI_LIST_200",
              "message": "알림 목록 조회에 성공했습니다.",
              "result": {
                "content": [
                  {
                    "id": 101,
                    "category": "TODO",
                    "title": "슬개골 영양제 체크가 필요해요",
                    "body": "오늘 09:00 · 미완료 상태예요",
                    "isRead": false,
                    "createdAt": "2026-08-20T09:00:00",
                    "ctaLabel": "할 일 보기",
                    "targetType": "TODO",
                    "targetId": 15
                  },
                  {
                    "id": 100,
                    "category": "AI",
                    "title": "AI 진료 요약이 완료됐어요",
                    "body": "진료 녹음 분석 결과 확인 가능",
                    "isRead": true,
                    "createdAt": "2026-08-19T18:10:00",
                    "ctaLabel": "요약 보기",
                    "targetType": "VISIT",
                    "targetId": 7
                  },
                  {
                    "id": 99,
                    "category": "COMMUNITY",
                    "title": "내 게시글에 댓글이 달렸어요",
                    "body": "한강공원 쪽 추천해요.",
                    "isRead": false,
                    "createdAt": "2026-08-19T12:00:00",
                    "ctaLabel": "글 보기",
                    "targetType": "POST",
                    "targetId": 10
                  },
                  {
                    "id": 98,
                    "category": "CHAT",
                    "title": "초코님의 메시지",
                    "body": "나눔 가능할까요?",
                    "isRead": false,
                    "createdAt": "2026-08-19T11:30:00",
                    "ctaLabel": "채팅 보기",
                    "targetType": "CHAT_ROOM",
                    "targetId": 12
                  }
                ],
                "nextCursor": "MjAyNi0wOC0xOVQxMTozMDowMHw5OA",
                "hasNext": true,
                "size": 20
              }
            }
            """;

    public static final String LIST_CHAT_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "NOTI_LIST_200",
              "message": "알림 목록 조회에 성공했습니다.",
              "result": {
                "content": [
                  {
                    "id": 98,
                    "category": "CHAT",
                    "title": "초코님의 메시지",
                    "body": "사진을 보냈습니다",
                    "isRead": false,
                    "createdAt": "2026-08-20T11:32:00",
                    "ctaLabel": "채팅 보기",
                    "targetType": "CHAT_ROOM",
                    "targetId": 12
                  }
                ],
                "nextCursor": null,
                "hasNext": false,
                "size": 20
              }
            }
            """;

    public static final String UNREAD_DESCRIPTION = """
            `isRead=false` 개수입니다. 코드 `NOTI_UNREAD_200`.
            마이페이지 홈 `unreadNotificationCount`와 같은 기준입니다. CHAT 미읽음도 포함합니다.
            """;

    public static final String UNREAD_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "NOTI_UNREAD_200",
              "message": "미읽음 알림 수 조회에 성공했습니다.",
              "result": { "count": 3 }
            }
            """;

    public static final String READ_DESCRIPTION = """
            본인 알림 한 건을 읽음 처리합니다. 코드 `NOTI_READ_200`.
            
            - 이미 읽은 알림을 다시 호출해도 200 (idempotent).
            - 없거나 타인 알림이면 `NOTI_404` (존재 여부를 숨깁니다).
            - 채팅방 메시지 읽음(`PATCH /chat/rooms/{roomId}/read`)과는 별개입니다.
            """;

    public static final String READ_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "NOTI_READ_200",
              "message": "알림을 읽음 처리했습니다.",
              "result": {
                "id": 98,
                "category": "CHAT",
                "title": "초코님의 메시지",
                "body": "나눔 가능할까요?",
                "isRead": true,
                "createdAt": "2026-08-19T11:30:00",
                "ctaLabel": "채팅 보기",
                "targetType": "CHAT_ROOM",
                "targetId": 12
              }
            }
            """;

    public static final String READ_ALL_DESCRIPTION = """
            내 미읽음 알림을 전부 읽음 처리합니다. 코드 `NOTI_READ_ALL_200`.
            `updatedCount`는 이번에 바뀐 행 수입니다. 이미 모두 읽었으면 0.
            """;

    public static final String READ_ALL_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "NOTI_READ_ALL_200",
              "message": "모든 알림을 읽음 처리했습니다.",
              "result": { "updatedCount": 5 }
            }
            """;

    public static final String NOTI_400_EXAMPLE = """
            {"isSuccess":false,"code":"NOTI_400","message":"알림 커서가 올바르지 않습니다.","result":null}
            """;
    public static final String NOTI_404_EXAMPLE = """
            {"isSuccess":false,"code":"NOTI_404","message":"알림을 찾을 수 없습니다.","result":null}
            """;
}

package com.gaguraczi.paw.domain.chat.controller;

/**
 * Swagger annotation constants for {@link ChatController}. Documentation only.
 */
public final class ChatApiDocs {

    private ChatApiDocs() {
    }

    public static final String JWT_401_1_DESCRIPTION = "JWT_401_1. 토큰 만료 또는 미인증.";
    public static final String JWT_401_1_EXAMPLE = """
            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
            """;

    public static final String TAG_DESCRIPTION = """
            장터(MARKET) 게시글 기반 1:1 채팅 API입니다. JWT Bearer 필수 (`Authorization: Bearer {accessToken}`).
            WebSocket이 없습니다. 실시간은 채팅방 재진입·메시지 목록 새로고침과 아래 FCM으로 맞춥니다.
            
            ## 권한
            - 참여자(판매자 seller / 구매자 buyer)만 방·메시지를 볼 수 있습니다. 아니면 `CHAT_403_2`.
            - 없는 방은 `CHAT_404_1`. 존재 여부를 숨기지 않습니다.
            
            ## 화면 순서
            1. 장터 글에서 `POST /chat/rooms` `{ "postId": 33 }` → `roomId`를 받습니다. 이미 있으면 같은 방을 반환합니다.
            2. `GET /chat/rooms/{roomId}` 로 상대·게시글 카드를 그리고, `GET /chat/rooms/{roomId}/messages` 로 대화를 채웁니다.
            3. 전송은 `POST /chat/rooms/{roomId}/messages` (multipart). 성공 후 목록 맨 앞에 붙이거나 방을 다시 조회합니다.
            4. 방을 열면(또는 스크롤로 최신까지 보면) `PATCH /chat/rooms/{roomId}/read` 로 읽음 처리합니다.
            5. 채팅 탭 목록은 `GET /chat/rooms` 입니다. `unreadCount`·미리보기를 그립니다.
            
            ## 방 규칙
            - 구매자만 방을 만듭니다. 요청자가 buyer, 글 작성자가 seller 입니다.
            - 본인 글에는 방을 만들 수 없습니다 (`CHAT_403_1`).
            - 소통글 등 MARKET이 아닌 글은 `CHAT_400_1`.
            - unique는 `(postId, buyerUid)` 입니다. 같은 글·같은 구매자면 방이 하나입니다.
            - `postId`는 FK가 아닙니다. 글이 삭제돼도 대화는 남고, 상세/목록의 `post.deleted=true` 입니다.
            
            ## 메시지
            - `TEXT`: `data.content` 필수. 빈 문자열이면 `CHAT_400_3`.
            - `IMAGE`: `image` 파트 필수. 커뮤니티와 동일하게 **5MB**, JPEG/PNG/GIF/WEBP/HEIC/HEIF.
            - 목록은 **최신 → 과거** `messageId` 내림차순입니다. `nextCursor`는 opaque 값이며 변조하면 `CHAT_400_2`.
            - 방 목록 `lastMessagePreview`: 텍스트는 최대 100자, 이미지는 `사진을 보냈습니다`.
            
            ## 읽음·미읽음
            - 읽음은 메시지 단위가 아니라 방 단위 `lastReadMessageId` 입니다. 더 큰 ID만 갱신됩니다.
            - `unreadCount`는 상대가 보낸, `lastReadMessageId`보다 큰 메시지 수입니다. 한 번도 안 읽었으면 상대 메시지 전체입니다.
            - 전송 API는 읽음 처리를 하지 않습니다. 보낸 사람은 별도로 read를 호출하세요.
            
            ## 채팅 알림 (FCM + 인박스)
            전송이 **커밋된 뒤** 상대방에게만 발송합니다. 이 API 200과 알림 성공은 별개입니다. 알림 실패해도 메시지는 저장됩니다.
            
            상대가 알림을 받으려면:
            1. `PUT /users/me/push-token` 으로 FCM 토큰이 있어야 푸시가 갑니다. 없으면 인박스만(설정이 켜진 경우) 쌓입니다.
            2. `PATCH /mypage/notifications/settings` 의 **`chatAlarm: true`**. 기본값은 **false** 입니다.
            3. 방해 금지 시간이면 인박스만 남기고 FCM은 생략합니다. 채팅은 건강 이상처럼 DND를 뚫지 않습니다.
            
            인박스 (`GET /notifications`, `category=CHAT`):
            | 필드 | 값 |
            |---|---|
            | title | `{닉네임}님의 메시지`. 닉네임이 없으면 `새 메시지` |
            | body | 미리보기(텍스트 최대 100자 또는 `사진을 보냈습니다`) |
            | targetType | `CHAT_ROOM` |
            | targetId | `roomId` |
            | ctaLabel | `채팅 보기` |
            
            FCM data payload (값은 모두 **문자열**):
            | 키 | 예 | 의미 |
            |---|---|---|
            | type | `CHAT_MESSAGE` | 채팅 메시지. 다른 알림의 type과 구분 |
            | category | `CHAT` | 인박스 카테고리와 동일 |
            | roomId | `"12"` | 채팅방. 탭 시 `GET /chat/rooms/{roomId}` |
            | postId | `"33"` | 장터 글 |
            | senderId | UUID 문자열 | 보낸 사람 |
            | messageId | `"501"` | 방금 저장한 메시지 |
            
            알림 문구 예: 제목 `초코님의 메시지` / 본문 `나눔 가능할까요?`
            
            ## 관련 API
            - 알림 목록: `GET /notifications?category=CHAT`
            - 알림 설정: `GET`/`PATCH /mypage/notifications/settings` (`chatAlarm`)
            - 푸시 토큰: `PUT /users/me/push-token`
            """;

    public static final String CREATE_DESCRIPTION = """
            장터 글 기준 1:1 방을 준비합니다. 코드 `CHAT_ROOM_CREATE_200`.
            
            - `postId` + 요청자(buyer) 조합이 있으면 **같은 `roomId`** 를 반환합니다. 새로 만들어도, 재호출이어도 200입니다.
            - 판매자(글 작성자)는 이 API로 자기 글 방을 만들 수 없습니다. 구매자가 만든 뒤 목록/상세로 들어갑니다.
            - 응답은 `roomId`·`createdAt`만 있습니다. 상대·글 카드는 `GET /chat/rooms/{roomId}` 를 치세요.
            """;

    public static final String CREATE_REQ_EXAMPLE = """
            {
              "postId": 33
            }
            """;

    public static final String CREATE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_ROOM_CREATE_200",
              "message": "채팅방이 준비되었습니다.",
              "result": {
                "roomId": 12,
                "createdAt": "2026-08-20T11:00:00"
              }
            }
            """;

    public static final String LIST_DESCRIPTION = """
            내가 참여한 방을 마지막 메시지 시각 내림차순으로 줍니다. 코드 `CHAT_ROOM_LIST_200`.
            
            - size 기본 20, 최대 50. 1 미만이면 20.
            - `cursor`는 이전 응답 `nextCursor`를 그대로 넣습니다. 없거나 빈 값이면 첫 페이지입니다.
            - `unreadCount`는 상대 메시지 기준입니다. 내가 보낸 건 세지 않습니다.
            - 글이 삭제됐으면 `post.deleted=true` 이고 title/price 등은 null 입니다. 방은 목록에 남습니다.
            - 메시지가 한 번도 없으면 `lastMessagePreview`는 null 일 수 있습니다. `lastMessageAt`은 방 생성 시각입니다.
            """;

    public static final String LIST_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_ROOM_LIST_200",
              "message": "채팅방 목록 조회에 성공했습니다.",
              "result": {
                "content": [
                  {
                    "roomId": 12,
                    "opponent": {
                      "uid": "11111111-1111-1111-1111-111111111111",
                      "nickname": "초코",
                      "profileUrl": "https://cdn.example.com/users/choco.jpg"
                    },
                    "post": {
                      "postId": 33,
                      "title": "사료 나눔합니다",
                      "thumbnailUrl": "https://cdn.example.com/posts/33.jpg",
                      "price": 0,
                      "priceNegotiable": false,
                      "marketStatus": "IN_PROGRESS",
                      "deleted": false
                    },
                    "lastMessagePreview": "나눔 가능할까요?",
                    "lastMessageAt": "2026-08-20T11:30:00",
                    "unreadCount": 2
                  }
                ],
                "nextCursor": null,
                "hasNext": false,
                "size": 20
              }
            }
            """;

    public static final String DETAIL_DESCRIPTION = """
            채팅방 상단 카드입니다. 코드 `CHAT_ROOM_DETAIL_200`.
            
            - `opponent`는 나 아닌 참여자입니다.
            - `post`는 요청 시점의 장터 글입니다. 가격·`marketStatus`가 바뀌면 이 API에서 최신값이 나옵니다.
            - 글이 없으면 `post.deleted=true`, `post.postId`만 채우고 나머지 카드 필드는 null 입니다. 대화는 유지합니다.
            """;

    public static final String DETAIL_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_ROOM_DETAIL_200",
              "message": "채팅방 조회에 성공했습니다.",
              "result": {
                "roomId": 12,
                "opponent": {
                  "uid": "11111111-1111-1111-1111-111111111111",
                  "nickname": "초코",
                  "profileUrl": "https://cdn.example.com/users/choco.jpg"
                },
                "post": {
                  "postId": 33,
                  "title": "사료 나눔합니다",
                  "thumbnailUrl": "https://cdn.example.com/posts/33.jpg",
                  "price": 0,
                  "priceNegotiable": false,
                  "marketStatus": "IN_PROGRESS",
                  "deleted": false
                }
              }
            }
            """;

    public static final String DETAIL_DELETED_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_ROOM_DETAIL_200",
              "message": "채팅방 조회에 성공했습니다.",
              "result": {
                "roomId": 12,
                "opponent": {
                  "uid": "11111111-1111-1111-1111-111111111111",
                  "nickname": "초코",
                  "profileUrl": "https://cdn.example.com/users/choco.jpg"
                },
                "post": {
                  "postId": 33,
                  "title": null,
                  "thumbnailUrl": null,
                  "price": null,
                  "priceNegotiable": null,
                  "marketStatus": null,
                  "deleted": true
                }
              }
            }
            """;

    public static final String MESSAGES_DESCRIPTION = """
            메시지 목록입니다. 코드 `CHAT_MESSAGE_LIST_200`.
            
            - **최신부터 과거**로 내려갑니다 (`messageId` DESC). 첫 페이지는 cursor 없이 호출합니다.
            - size 기본 30, 최대 50. 1 미만이면 30.
            - `mine=true` 이면 내가 보낸 메시지입니다. 말풍선 방향을 이 값으로 그리세요.
            - TEXT: `content` 있고 `imageUrl`은 null.
            - IMAGE: `imageUrl` 있고 `content`는 null.
            - `nextCursor`는 이 페이지에서 가장 오래된 `messageId`를 인코딩한 값입니다. 더 과거를 불러올 때 그대로 넣습니다.
            """;

    public static final String MESSAGES_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_MESSAGE_LIST_200",
              "message": "메시지 목록 조회에 성공했습니다.",
              "result": {
                "content": [
                  {
                    "messageId": 501,
                    "senderId": "22222222-2222-2222-2222-222222222222",
                    "mine": true,
                    "type": "TEXT",
                    "content": "네 가능해요",
                    "imageUrl": null,
                    "sentAt": "2026-08-20T11:31:00"
                  },
                  {
                    "messageId": 500,
                    "senderId": "11111111-1111-1111-1111-111111111111",
                    "mine": false,
                    "type": "IMAGE",
                    "content": null,
                    "imageUrl": "https://cdn.example.com/chat/500.jpg",
                    "sentAt": "2026-08-20T11:30:20"
                  },
                  {
                    "messageId": 499,
                    "senderId": "11111111-1111-1111-1111-111111111111",
                    "mine": false,
                    "type": "TEXT",
                    "content": "나눔 가능할까요?",
                    "imageUrl": null,
                    "sentAt": "2026-08-20T11:30:00"
                  }
                ],
                "nextCursor": "NDk5",
                "hasNext": true,
                "size": 30
              }
            }
            """;

    public static final String SEND_DESCRIPTION = """
            메시지를 저장하고, 커밋 후 상대에게 채팅 알림을 시도합니다. 코드 `CHAT_MESSAGE_SEND_200`.
            
            ## Request (multipart/form-data)
            | part | Content-Type | 필수 | 내용 |
            |---|---|---|---|
            | data | application/json | 예 | `{ "type": "TEXT", "content": "네 가능해요" }` 또는 `{ "type": "IMAGE" }` |
            | image | 파일 | IMAGE일 때 예 | JPEG/PNG/GIF/WEBP/HEIC/HEIF. 최대 5MB. TEXT면 보내지 않습니다. |
            
            ## TEXT
            - `data.content`가 없거나 공백이면 `CHAT_400_3`.
            
            ## IMAGE
            - `image`가 없거나 비어 있으면 `CHAT_400_4`.
            - 용량 초과 `COMMUNITY_400_9`, 미지원 포맷 `COMMUNITY_400_10`.
            
            ## 알림
            - 응답 200은 **메시지 저장 성공**입니다. 상대 `chatAlarm`이 꺼져 있거나 방해 금지·토큰 없음이면 푸시는 안 갑니다.
            - 알림 탭은 `roomId`로 이 방을 열면 됩니다. FCM data는 태그 설명의 `CHAT_MESSAGE` 표를 보세요.
            """;

    public static final String SEND_TEXT_DATA_EXAMPLE = """
            {
              "type": "TEXT",
              "content": "네 가능해요"
            }
            """;

    public static final String SEND_IMAGE_DATA_EXAMPLE = """
            {
              "type": "IMAGE"
            }
            """;

    public static final String SEND_TEXT_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_MESSAGE_SEND_200",
              "message": "메시지가 전송되었습니다.",
              "result": {
                "messageId": 501,
                "senderId": "22222222-2222-2222-2222-222222222222",
                "mine": true,
                "type": "TEXT",
                "content": "네 가능해요",
                "imageUrl": null,
                "sentAt": "2026-08-20T11:31:00"
              }
            }
            """;

    public static final String SEND_IMAGE_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_MESSAGE_SEND_200",
              "message": "메시지가 전송되었습니다.",
              "result": {
                "messageId": 502,
                "senderId": "22222222-2222-2222-2222-222222222222",
                "mine": true,
                "type": "IMAGE",
                "content": null,
                "imageUrl": "https://cdn.example.com/chat/502.jpg",
                "sentAt": "2026-08-20T11:32:00"
              }
            }
            """;

    public static final String READ_DESCRIPTION = """
            이 방에서 내가 읽은 마지막 메시지 ID를 올립니다. 코드 `CHAT_ROOM_READ_200`. `result`는 null 입니다.
            
            - `lastReadMessageId`는 **이 방의 메시지**여야 합니다. 아니면 `CHAT_404_3`.
            - 이미 더 큰 ID를 읽었으면 그대로 둡니다 (뒤로 줄이지 않음).
            - 방을 열었을 때 목록 첫 메시지(가장 최신) ID를 보내면 됩니다.
            """;

    public static final String READ_REQ_EXAMPLE = """
            {
              "lastReadMessageId": 501
            }
            """;

    public static final String READ_200_EXAMPLE = """
            {
              "isSuccess": true,
              "code": "CHAT_ROOM_READ_200",
              "message": "읽음 처리되었습니다.",
              "result": null
            }
            """;

    public static final String CHAT_400_1_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_400_1","message":"장터 게시글에만 채팅을 시작할 수 있습니다.","result":null}
            """;
    public static final String CHAT_400_2_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_400_2","message":"유효하지 않은 커서입니다.","result":null}
            """;
    public static final String CHAT_400_3_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_400_3","message":"텍스트 메시지에는 내용이 필요합니다.","result":null}
            """;
    public static final String CHAT_400_4_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_400_4","message":"이미지 메시지에는 이미지 파일이 필요합니다.","result":null}
            """;
    public static final String COMMUNITY_400_9_EXAMPLE = """
            {"isSuccess":false,"code":"COMMUNITY_400_9","message":"이미지 용량은 5MB 이하여야 합니다.","result":null}
            """;
    public static final String COMMUNITY_400_10_EXAMPLE = """
            {"isSuccess":false,"code":"COMMUNITY_400_10","message":"지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다.","result":null}
            """;
    public static final String CHAT_403_1_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_403_1","message":"본인 게시글에는 채팅방을 생성할 수 없습니다.","result":null}
            """;
    public static final String CHAT_403_2_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_403_2","message":"채팅방 참여자만 접근할 수 있습니다.","result":null}
            """;
    public static final String CHAT_404_1_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_404_1","message":"채팅방을 찾을 수 없습니다.","result":null}
            """;
    public static final String CHAT_404_2_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_404_2","message":"게시글을 찾을 수 없습니다.","result":null}
            """;
    public static final String CHAT_404_3_EXAMPLE = """
            {"isSuccess":false,"code":"CHAT_404_3","message":"해당 채팅방의 메시지를 찾을 수 없습니다.","result":null}
            """;
}

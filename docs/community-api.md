# 커뮤니티 API 명세서

> 기준 코드: `CommunityController`, `CommentController`, `CommunityLikeService`  
> 피드 API는 `COMMUNICATION`(소통), `MARKET`(장터)만 지원. `REVIEW`는 태그 시드만 존재하며 피드/작성 API 범위 밖.

---

## 1. API 목록

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| GET | `/community-tags` | JWT | 태그(칩) 목록 |
| GET | `/communities` | JWT | 게시글 목록 (커서 슬라이딩) |
| GET | `/communities/{postId}` | JWT | 게시글 상세 (조회수 Redis 증가) |
| POST | `/communities` | JWT | 게시글 작성 (`multipart/form-data`) |
| PUT | `/communities/{postId}` | JWT | 게시글 수정 (`multipart/form-data`, 작성자만) |
| DELETE | `/communities/{postId}` | JWT | 게시글 삭제 (작성자만) |
| PATCH | `/communities/{postId}/likes` | JWT | 좋아요 토글 |
| GET | `/communities/{postId}/comments` | JWT | 댓글 목록 (flat + parentId, 커서) |
| POST | `/communities/{postId}/comments` | JWT | 댓글/대댓글 작성 |
| PUT | `/comments/{commentId}` | JWT | 댓글 수정 (작성자만) |
| DELETE | `/comments/{commentId}` | JWT | 댓글 soft delete (작성자만) |

### Security

- 커뮤니티 경로에 별도 `permitAll` 없음 → `anyRequest().authenticated()` (JWT 필수)
- Authorization: `Bearer {accessToken}`

---

## 2. 공통

### 응답 래퍼 `ApiResponse<T>`

```json
{
  "isSuccess": true,
  "code": "COMMUNITY_LIST_200",
  "message": "커뮤니티 목록 조회에 성공했습니다.",
  "result": {}
}
```

### 커서 페이지 `CursorPageRes<T>`

```json
{
  "content": [],
  "nextCursor": "base64url...",
  "hasNext": true,
  "size": 20
}
```

- `size` 기본 20, 최대 50 (`size` 미입력/1 미만 → 20)
- `cursor`는 서버가 내려준 `nextCursor`를 그대로 다음 요청에 전달
- sort를 바꾸면 기존 cursor는 무효 (`COMMUNITY_400_2`)

### Enum

| 이름 | 값 |
|------|-----|
| `PostType` | `COMMUNICATION`, `MARKET`, `REVIEW` (API는 COMMUNICATION/MARKET만) |
| `CommunitySort` | `LATEST`(기본), `LIKE`, `VIEW`, `COMMENT` |
| `MarketStatus` | `IN_PROGRESS`, `RESERVED`, `COMPLETED` |
| `MarketTradeType` | `SHARE`, `SELL`, `EXCHANGE`, `WANT` |
| `MarketTradeMethod` | `DIRECT`, `DELIVERY`, `CONTACTLESS_SHARE` |

### 이미지 규칙

- part 이름: `images` (0~5장)
- 파일당 최대 **5MB**
- 허용 형식: JPEG, PNG, GIF, WEBP, HEIC, HEIF
- 게시글당 사진 합계(유지 + 신규) 최대 **5장**
- 썸네일: 게시글당 **1개** (`isThumbnail`)

---

## 3. 태그

### `GET /community-tags`

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| `postType` | PostType | Y | `COMMUNICATION` \| `MARKET` |

**성공 코드:** `COMMUNITY_TAG_LIST_200`

```json
{
  "isSuccess": true,
  "code": "COMMUNITY_TAG_LIST_200",
  "message": "커뮤니티 태그 목록 조회에 성공했습니다.",
  "result": [
    {
      "tagId": 1,
      "tagName": "건강상담",
      "tagCode": "HEALTH_CONSULT",
      "postType": "COMMUNICATION",
      "sortOrder": 1
    }
  ]
}
```

### 시드 태그 (참고)

| postType | tagName | tagCode |
|----------|----------|---------|
| COMMUNICATION | 건강상담 | HEALTH_CONSULT |
| COMMUNICATION | 산책친구 | WALK_BUDDY |
| COMMUNICATION | 헌혈소식 | BLOOD_NEWS |
| COMMUNICATION | 동네정보 | LOCAL_INFO |
| MARKET | 사료·간식 | FOOD_SNACK |
| MARKET | 용품 | SUPPLIES |
| MARKET | 소모품 | CONSUMABLES |
| MARKET | 영양제 | SUPPLEMENT |
| MARKET | 기타 | OTHER |
| REVIEW | 산책 장소 / 병원 / 용품샵 / 미용실 | (시드만, 피드 API 미지원) |

---

## 4. 게시글 목록

### `GET /communities`

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| `postType` | PostType | Y | `COMMUNICATION` \| `MARKET` |
| `tagId` | Long | N | 태그 필터 |
| `status` | MarketStatus | N | 장터 상태 필터 (`MARKET`만 의미 있음) |
| `tradeType` | MarketTradeType | N | 장터 거래유형 필터 |
| `sort` | CommunitySort | N | 기본 `LATEST` |
| `cursor` | String | N | 이전 응답의 `nextCursor` |
| `size` | Integer | N | 기본 20, 최대 50 |

- `postType=COMMUNICATION`이면 `status`, `tradeType`은 무시됨

**성공 코드:** `COMMUNITY_LIST_200`

```json
{
  "isSuccess": true,
  "code": "COMMUNITY_LIST_200",
  "message": "커뮤니티 목록 조회에 성공했습니다.",
  "result": {
    "content": [
      {
        "postId": 10,
        "postType": "MARKET",
        "tagId": 5,
        "tagName": "사료·간식",
        "title": "사료 나눔합니다",
        "contentPreview": "본문 앞 80자...",
        "viewCount": 12,
        "likeCount": 3,
        "commentCount": 1,
        "authorNickname": "닉네임",
        "thumbnailUrl": "https://...",
        "tradeType": "SHARE",
        "marketStatus": "IN_PROGRESS",
        "price": null,
        "priceNegotiable": false,
        "regionName": "서울특별시 강남구",
        "createdAt": "2026-08-08T10:00:00"
      }
    ],
    "nextCursor": "...",
    "hasNext": true,
    "size": 20
  }
}
```

- `viewCount` / `likeCount`는 Redis 캐시 우선 (없으면 DB 값)

---

## 5. 게시글 상세

### `GET /communities/{postId}`

| Path | 타입 | 설명 |
|------|------|------|
| `postId` | Long | 게시글 ID |

**성공 코드:** `COMMUNITY_DETAIL_200`

- 조회 시 Redis 조회수 증가 (동일 유저 중복 조회는 스토어 정책에 따라 디듀프)
- `likedByMe`: 로그인 유저 기준 (JWT 필수이므로 보통 `true`/`false`)

```json
{
  "isSuccess": true,
  "code": "COMMUNITY_DETAIL_200",
  "message": "커뮤니티 상세 조회에 성공했습니다.",
  "result": {
    "postId": 10,
    "postType": "COMMUNICATION",
    "tagId": 1,
    "tagName": "건강상담",
    "tagCode": "HEALTH_CONSULT",
    "title": "제목",
    "content": "본문",
    "hashTags": ["산책", "건강"],
    "photos": [
      {
        "photoId": 1,
        "url": "https://...",
        "isThumbnail": true,
        "sortOrder": 0
      }
    ],
    "viewCount": 13,
    "likeCount": 3,
    "commentCount": 2,
    "likedByMe": false,
    "authorNickname": "닉네임",
    "tradeType": null,
    "marketStatus": null,
    "price": null,
    "priceNegotiable": false,
    "expiryDate": null,
    "tradeMethod": null,
    "regionName": null,
    "createdAt": "2026-08-08T10:00:00"
  }
}
```

---

## 6. 게시글 작성

### `POST /communities`

`Content-Type: multipart/form-data`

| Part | 타입 | 필수 | 설명 |
|------|------|------|------|
| `data` | JSON (`CommunityCreateReq`) | Y | `Content-Type: application/json` |
| `images` | file[] | N | 0~5장 |

#### `data` 필드

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `postType` | PostType | Y | `COMMUNICATION` \| `MARKET` |
| `tagId` | Long | Y | 해당 postType 태그 |
| `title` | String | Y | 최대 200자 |
| `content` | String | Y | |
| `hashTags` | String[] | N | |
| `thumbnailIndex` | Integer | N | 신규 업로드 중 썸네일 인덱스(0-based). 없으면 0 |
| `tradeType` | MarketTradeType | MARKET 필수 | |
| `tradeMethod` | MarketTradeMethod | MARKET 필수 | |
| `price` | Long | N | 장터 |
| `priceNegotiable` | Boolean | N | |
| `expiryDate` | LocalDate | N | `yyyy-MM-dd` |
| `regionCode` | String | N | 법정동 코드. 있으면 유효해야 함 |

**성공 코드:** `COMMUNITY_CREATE_200` — `result`는 `CommunityDetailRes`

#### 예시 (`data`)

소통:

```json
{
  "postType": "COMMUNICATION",
  "tagId": 1,
  "title": "산책 친구 구해요",
  "content": "주말에 같이 산책하실 분",
  "hashTags": ["산책"],
  "thumbnailIndex": 0
}
```

장터:

```json
{
  "postType": "MARKET",
  "tagId": 5,
  "title": "사료 나눔",
  "content": "개봉만 했습니다",
  "tradeType": "SHARE",
  "tradeMethod": "DIRECT",
  "price": null,
  "priceNegotiable": false,
  "expiryDate": "2026-09-01",
  "regionCode": "1168010100",
  "thumbnailIndex": 0
}
```

- 장터 생성 시 `marketStatus`는 서버가 `IN_PROGRESS`로 설정

---

## 7. 게시글 수정

### `PUT /communities/{postId}`

`Content-Type: multipart/form-data` (작성자만)

| Part | 타입 | 필수 | 설명 |
|------|------|------|------|
| `data` | JSON (`CommunityUpdateReq`) | Y | |
| `images` | file[] | N | 신규 추가 사진 |

#### `data` 필드

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `tagId` | Long | Y | |
| `title` | String | Y | 최대 200자 |
| `content` | String | Y | |
| `hashTags` | String[] | N | |
| `keepPhotoUrls` | String[] | N | 유지할 기존 사진 URL. 목록에 없는 기존 사진은 삭제 |
| `thumbnailUrl` | String | N | 최종 썸네일로 쓸 **기존** 사진 URL |
| `thumbnailIndex` | Integer | N | `thumbnailUrl`이 없을 때, keep+신규 합친 목록 기준 인덱스 |
| `tradeType` | MarketTradeType | MARKET 필수 | |
| `marketStatus` | MarketStatus | N | 장터 상태 변경 |
| `price` / `priceNegotiable` / `expiryDate` / `tradeMethod` / `regionCode` | | N | 장터 필드 |

- 최종 사진 수 = `keepPhotoUrls` + 신규 `images` ≤ 5
- `postType`은 수정 불가

**성공 코드:** `COMMUNITY_UPDATE_200`

---

## 8. 게시글 삭제

### `DELETE /communities/{postId}`

작성자만. S3 사진·Redis 카운트 정리 포함.

**성공 코드:** `COMMUNITY_DELETE_200` — `result: null`

---

## 9. 좋아요 토글

### `PATCH /communities/{postId}/likes`

Body 없음. 없으면 좋아요 / 있으면 취소.

**성공 코드:** `LIKE_TOGGLE_200`

```json
{
  "isSuccess": true,
  "code": "LIKE_TOGGLE_200",
  "message": "좋아요 상태가 변경되었습니다.",
  "result": {
    "liked": true,
    "likeCount": 4
  }
}
```

- `likeCount`는 Redis 절대 카운트 (주기적으로 DB flush)

---

## 10. 댓글

### `GET /communities/{postId}/comments`

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| `cursor` | String | N | |
| `size` | Integer | N | 기본 20, 최대 50 |

- flat 목록 + `parentId`로 트리 구성 (중첩 대댓글 허용)
- soft delete된 댓글도 포함 (`deleted: true`, `content: null`)

**성공 코드:** `COMMENT_LIST_200`

```json
{
  "result": {
    "content": [
      {
        "commentId": 1,
        "postId": 10,
        "parentId": null,
        "content": "댓글입니다",
        "deleted": false,
        "authorNickname": "닉네임",
        "createdAt": "2026-08-08T11:00:00"
      },
      {
        "commentId": 2,
        "postId": 10,
        "parentId": 1,
        "content": "대댓글",
        "deleted": false,
        "authorNickname": "다른유저",
        "createdAt": "2026-08-08T11:05:00"
      }
    ],
    "nextCursor": "...",
    "hasNext": false,
    "size": 20
  }
}
```

### `POST /communities/{postId}/comments`

```json
{
  "content": "댓글 내용",
  "parentId": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `content` | String | Y | |
| `parentId` | Long | N | 있으면 대댓글. 부모는 같은 게시글이어야 함 |

**성공 코드:** `COMMENT_CREATE_200`

### `PUT /comments/{commentId}`

```json
{ "content": "수정된 내용" }
```

**성공 코드:** `COMMENT_UPDATE_200` (작성자만)

### `DELETE /comments/{commentId}`

soft delete. **성공 코드:** `COMMENT_DELETE_200` — `result: null` (작성자만)

---

## 11. 성공 / 에러 코드

### Success

| code | HTTP | 메시지 |
|------|------|--------|
| `COMMUNITY_TAG_LIST_200` | 200 | 커뮤니티 태그 목록 조회에 성공했습니다. |
| `COMMUNITY_LIST_200` | 200 | 커뮤니티 목록 조회에 성공했습니다. |
| `COMMUNITY_DETAIL_200` | 200 | 커뮤니티 상세 조회에 성공했습니다. |
| `COMMUNITY_CREATE_200` | 200 | 게시글이 등록되었습니다. |
| `COMMUNITY_UPDATE_200` | 200 | 게시글이 수정되었습니다. |
| `COMMUNITY_DELETE_200` | 200 | 게시글이 삭제되었습니다. |
| `COMMENT_LIST_200` | 200 | 댓글 목록 조회에 성공했습니다. |
| `COMMENT_CREATE_200` | 200 | 댓글이 등록되었습니다. |
| `COMMENT_UPDATE_200` | 200 | 댓글이 수정되었습니다. |
| `COMMENT_DELETE_200` | 200 | 댓글이 삭제되었습니다. |
| `LIKE_TOGGLE_200` | 200 | 좋아요 상태가 변경되었습니다. |

### Error

| code | HTTP | 메시지 |
|------|------|--------|
| `COMMUNITY_404` | 404 | 게시글을 찾을 수 없습니다. |
| `COMMUNITY_404_2` | 404 | 커뮤니티 태그를 찾을 수 없습니다. |
| `COMMUNITY_404_3` | 404 | 댓글을 찾을 수 없습니다. |
| `COMMUNITY_404_4` | 404 | 지역을 찾을 수 없습니다. |
| `COMMUNITY_400_1` | 400 | 지원하지 않는 게시글 유형입니다. |
| `COMMUNITY_400_2` | 400 | 유효하지 않은 커서입니다. |
| `COMMUNITY_400_3` | 400 | 댓글 순환 참조는 허용되지 않습니다. |
| `COMMUNITY_400_4` | 400 | 다른 게시글의 댓글에는 대댓글을 달 수 없습니다. |
| `COMMUNITY_400_5` | 400 | 게시글 유형과 태그가 일치하지 않습니다. |
| `COMMUNITY_400_6` | 400 | 장터 게시글에 필수 값이 누락되었습니다. |
| `COMMUNITY_400_7` | 400 | 사진은 최대 5장까지 업로드할 수 있습니다. |
| `COMMUNITY_400_8` | 400 | 이미지 파일이 비어 있습니다. |
| `COMMUNITY_400_9` | 400 | 이미지 용량은 5MB 이하여야 합니다. |
| `COMMUNITY_400_10` | 400 | 지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다. |
| `COMMUNITY_400_11` | 400 | 썸네일로 지정할 사진을 찾을 수 없습니다. |
| `COMMUNITY_400_12` | 400 | 댓글 내용이 올바르지 않습니다. |
| `COMMUNITY_403` | 403 | 권한이 없습니다. |
| `JWT_401` | 401 | 인증이 필요합니다. |

---

## 12. 참고 구현 메모

- 조회수/좋아요 수는 Redis absolute counter + dirty set → 스케줄러가 약 1분마다 DB flush
- 게시글/댓글 목록은 offset이 아닌 **keyset(cursor) 슬라이딩**
- 댓글은 self-join (`parent`). 순환은 앱 검증 + DB 트리거로 차단
- Swagger: `/swagger-ui.html` 태그 `communities`, `comments`

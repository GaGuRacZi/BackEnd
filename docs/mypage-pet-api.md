# 마이페이지 & 펫관리 API 명세

범위: 마이페이지(구독/결제 제외) + 펫관리. 결제 실행/결제수단/결제내역/PG연동은 이번 범위에서 제외한다.
공통: 인증 필요 API는 `Authorization: Bearer {AccessToken}` (JWT). 응답은 `ApiResponse<T>` 래퍼(`isSuccess/code/message/result`) 고정.
공통 에러: `COMMON_400`, `JWT_401_1`(만료), `JWT_403_2`(유효하지 않음)은 전 API 공통이라 아래 표에서는 도메인 고유 에러만 기재한다.

---

## A. 마이페이지 (domain/mypage 신규 패키지)

### A1. 마이페이지 홈 요약
- `GET /mypage/home` (JWT)
- 기존 `UserService.getMyProfile`, `PetRepository.findFirstByUserAndIsMainTrue` 재사용해서 조립만.
- Response
```json
{
  "nickname": "초코엄마",
  "regionName": "서울 강남구",
  "mainPet": { "petId": 1, "petName": "초코", "profileUrl": "..." },
  "subscribe": { "planName": "BASIC", "active": true }
}
```
- `subscribe`는 `User.subscribe`(SubscribeType) 값을 그대로 표시용으로 매핑(요금제 상세/변경 API는 이번 범위 제외).
- 에러: 없음(로그인 유저 기준 항상 200).

### A2. 프로필 상세 조회
- `GET /mypage/profile` (JWT)
- 기존 `UserController.GET /users/me`와 거의 동일하지만 "연동 계정 목록"이 추가로 필요 → `OAuthRepository.findAllByUser` 신규 메서드 추가해서 조립.
- Response: 기존 `UserProfileRes` 필드 + `linkedAccounts: [{ socialType, linkedAt }]`
- 에러: 없음.

### A3. 프로필 수정 / 사진 업로드·삭제
- 닉네임/한줄소개/지역 텍스트 수정: **기존 재사용** `PUT /users/me/profile` (multipart) 그대로.
- 사진만 업로드/교체: 기존 재사용 (동일 API의 image part).
- 사진 삭제: **신규** `DELETE /mypage/profile/image` (JWT) — `S3Utils.deleteQuietly` + `User.updateProfileImage(null, null)`.
  - Response: `{ "profileUrl": null }`
  - 에러: 없음(이미지가 없어도 idempotent 204/200).

### A4. 지역 설정
- 주소 검색 결과 선택 저장: **기존 재사용** — `RegionController.GET /regions/search` 로 검색 후, 선택한 `regionCode`를 저장하는 API가 없다면 신규 `PATCH /mypage/region { regionCode }` 필요(LocationService/LegalRegionRepository 재사용, User.updateLocation 호출, 위경도는 null 유지).
- 현재 위치 기반 자동 설정: **기존 재사용** `POST /location/user/cert?lat&lng`.

### A5. 계정 연동
- **기존 재사용**: `AuthController`의 `/auth/link/kakao`, `/auth/link/confirm/kakao`, `/auth/link/confirm/local`.

---

## B. 커뮤니티 활동 (domain/mypage, community 리포지토리에 쿼리만 추가)

### B1. 내가 작성한 글
- `GET /mypage/community/posts?postType={ALL|COMMUNICATION|MARKET|REVIEW}&cursor=&size=20` (JWT)
- `CommunityRepository`에 `findByUser(uid, postType, cursor...)` 메서드 추가(기존 목록 조회 쿼리와 동일 패턴, `uid` 조건만 추가). 커서 인코딩은 기존 `CommunityCursorCodec` 재사용.
- Response: `CursorPageRes<MyPostItemRes>` — `{ postId, postType, tagName, title, commentCount, likeCount(REVIEW는 rating), createdAt }`

### B2. 내가 찜한 글
- `GET /mypage/community/likes?cursor=&size=20` (JWT)
- `CommunityLikeRepository`에 `findByUser(uid, cursor...)` 추가. 응답 아이템 형태는 B1과 동일 `MyPostItemRes` 재사용.

### B3. 내가 댓글 단 글
- `GET /mypage/community/comments?cursor=&size=20` (JWT)
- `CommentRepository`에 `findByUser(uid, cursor...)` 추가(`isDel=false`만). 게시글당 최신 내 댓글 1건 기준으로 그룹핑.
- Response: `CursorPageRes<MyCommentItemRes>` — `{ postId, title, commentPreview, commentedAt }`

---

## C. 알림 설정 (domain/mypage 신규, 알림설정 엔티티 신규)

### 신규 엔티티: `NotificationSetting` (User 1:1)
필드: `uid`(PK/FK), `todoAlarm`, `healthAlarm`, `aiAnalysisAlarm`, `communityAlarm`, `chatAlarm`, `benefitAlarm` (모두 Boolean, 기본 true), `dndEnabled`(Boolean, 기본 false), `dndStart`(LocalTime, 기본 22:00), `dndEnd`(LocalTime, 기본 07:00).
- 온보딩 완료 시점(`AuthService` 온보딩 로직)에 기본값으로 1건 생성 필요 — 기존 온보딩 트랜잭션에 생성 호출 추가(일부 수정).
- `healthAlarm`은 방해금지 시간대 예외로 정책 고정(발송 로직은 이번 범위 밖, 필드/문서화만).

### C1. 알림 설정 조회
- `GET /mypage/notifications/settings` (JWT)
- 없으면 lazy 생성(기본값) 후 반환.

### C2. 알림 설정 수정(개별/일괄)
- `PATCH /mypage/notifications/settings` (JWT) — body에 있는 필드만 부분 반영(Pet.update 패턴과 동일한 null-safe 업데이트).
```json
{ "todoAlarm": false, "dndEnabled": true, "dndStart": "22:00", "dndEnd": "07:00" }
```
- 에러: `MYPAGE_400_1`(dndStart/dndEnd 둘 중 하나만 온 경우 등 유효성 실패)

---

## D. 공지사항 / 약관 / 문의

### D1. 약관 — **기존 재사용**
- `TermsController.GET /terms`, `GET /terms/{type}` 그대로. 동의 상태는 `UserAgreementRepository.findByUserAndTermsType` 조회 추가해 `agreed` 필드로 노출(D1-확장: `GET /mypage/terms` — 로그인 유저 동의 상태 포함 목록, 신규 얇은 래퍼).

### D2. 공지사항 (domain/mypage 신규 엔티티 `Notice`)
필드: `noticeId, title, content, viewCount, createdAt`. NEW 배지는 `createdAt`이 7일 이내인지로 판단(별도 컬럼 불필요).
- `GET /mypage/notices?keyword=&cursor=&size=20` (JWT 불필요, permitAll) — 커서 기반, `CursorPageRes<NoticeListItemRes>`
- `GET /mypage/notices/{noticeId}` (permitAll) — 상세 조회 시 `viewCount` 증가

### D3. 문의 (domain/mypage 신규 엔티티 `Inquiry`, `InquiryStatus{RECEIVED, IN_PROGRESS, ANSWERED}`)
필드: `inquiryId, user, inquiryType(enum), content, attachmentUrls(List<String>), status, answer(nullable), createdAt`
- `POST /mypage/inquiries` (JWT, multipart: data JSON + files 다중) — `S3Utils.uploadMultipartUnderDirectory("inquiry")` 재사용
- `GET /mypage/inquiries?cursor=&size=20` (JWT) — 내 문의 내역
- `GET /mypage/inquiries/{inquiryId}` (JWT, 본인 소유만)
- 에러: `MYPAGE_404_1`(문의 없음/본인 아님)

---

## E. 계정 관리

### E1. 로그아웃 — **기존 재사용**: `POST /auth/logout`

### E2. 회원 탈퇴
- `GET /mypage/withdrawal/preview` (JWT) — 탈퇴 전 확인 항목: 구독 이용중 여부(`User.subscribe != BASIC`), 진행중 거래(MARKET 게시글 중 `marketStatus`가 거래중인 것 존재 여부) 등 경고 플래그 반환.
- `DELETE /mypage/withdrawal` (JWT) — **soft delete**. `User`에 `isDeleted`(Boolean, 기본 false), `deletedAt`(LocalDateTime) 컬럼 추가, `User.withdraw()` 도메인 메서드 추가. 개인식별정보(email/name/nickname/profileUrl 등)는 익명화, `RefreshTokenRedisStore`에서 RT 즉시 삭제(AuthService 재사용). 채팅/커뮤니티 등 이미 생성된 콘텐츠는 삭제하지 않고 작성자 표시만 "탈퇴한 사용자"로 대체(조회 시 처리 — `User.isDeleted` 체크).
- `SecurityConfig`/`JwtAuthenticationFilter`에서 이후 요청은 `isDeleted=true` User면 인증 거부하도록 일부 수정 필요.
- 에러: `MYPAGE_400_2`(이미 탈퇴한 계정)

---

## F. 펫관리 (domain/pets, 엔티티는 기존처럼 users.entity 하위에 위치)

### F1. 반려동물 목록 조회
- `GET /pets` (JWT) — 신규. `PetRepository.findByUser` 재사용, `PetRes` 리스트(대표펫이 배열 첫 순서).
- Response: `PetRes[]`

### F2. 반려동물 단건 조회
- `GET /pets/{petId}` (JWT) — 신규, 본인 소유 검증(`PET_404`).

### F3. 대표 반려동물 전환
- `PATCH /pets/{petId}/main` (JWT) — 신규. 기존 대표(`findFirstByUserAndIsMainTrue`) `setMain(false)` → 대상 `setMain(true)`.
- 에러: `PET_404`

### F4. 반려동물 등록 — **기존 재사용**: `POST /pets` (multipart). 의료정보(혈액형) 파라미터만 `PetCreateReq`에 옵션 필드로 추가(일부 수정).

### F5. 반려동물 수정 — **기존 재사용**: `PUT /pets/{petId}`. 혈액형 필드 `PetUpdateReq`에 추가(일부 수정).

### F6. 반려동물 삭제
- `DELETE /pets/{petId}` (JWT) — 신규. Hard delete + cascade(동물등록증/의료정보 매핑은 `orphanRemoval`/FK `ON DELETE CASCADE`로 함께 삭제, AI 분석 이력은 저장소에 없어 대상 아님). 대표펫 삭제 시 남은 펫 중 최신순 1마리를 자동으로 대표 지정.
- 에러: `PET_404`

### F7. 혈업형(의료정보) — `Pet`에 컬럼 추가
- `Pet.bloodType`(String, nullable) 컬럼 추가. 저장 시 `petType`에 따라 검증:
  - DOG: `NONE | DEA_1_1_POSITIVE | DEA_1_1_NEGATIVE | UNKNOWN`
  - CAT: `NONE | A | B | AB | UNKNOWN`
- 신규 enum 2종 `domain/users/enums/DogBloodType`, `CatBloodType`. `PetErrorCode.PET_BLOOD_TYPE_MISMATCH`(종에 안맞는 혈액형 값) 추가.
- 등록/수정 API(F4/F5)에 포함, 별도 API 없음.

### F8. 동물등록증 (`/pets/{petId}/registration`, 신규 엔티티 `PetRegistration` 1:1)
필드: `petId(PK/FK), guardianName, registrationNumber, photoS3Key, photoUrl`
- `GET /pets/{petId}/registration` (JWT) — 없으면 `PET_REGISTRATION_404`
- `PUT /pets/{petId}/registration` (JWT, multipart: data + photo 선택) — upsert(있으면 수정, 없으면 생성)
- 에러: `PET_404`(펫 없음/본인 아님)

### F9. 먹거리·관리 정보 (공통 패턴 3종)
공통 마스터 코드 엔티티 신규 3종 — `species` 컬럼(DOG/CAT/null=공통)으로 종별 분기:
- `IngredientCode`(id, name, species) — 피해야 할 원료
- `SurgeryCode`(id, name, species) — 수술 이력
- `CareAreaCode`(id, name, species) — 관리 부위

매핑 테이블(다대다) — petId + codeId 복합키:
- `PetExcludedIngredient`, `PetSurgeryHistory`, `PetCareArea`

**공통 API 패턴** (도메인 `{domain}` = `ingredients` | `surgeries` | `care-areas`):
- `GET /pets/{domain}/codes?keyword=&petType={DOG|CAT}` (JWT) — 마스터 코드 검색(이름 LIKE + species 필터, species=null인 공통 코드는 항상 포함)
- `GET /pets/{petId}/{domain}` (JWT) — 반려동물이 현재 선택한 코드 목록
- `PUT /pets/{petId}/{domain}` (JWT) — 전체 갈아끼우기(`{ codeIds: [1,2,3] }`) — 기존 매핑 전체 삭제 후 재삽입. diff 방식 대신 갈아끼우기로 통일(요청/응답이 항상 전체 목록이라 클라이언트 구현이 단순해짐).
- 마스터 데이터는 AI 분석 도메인이 저장소에 없으므로 마이페이지 쪽에서 시드(`config/PetCodeDataLoader`, `Terms`/`Breed` 데이터로더와 동일 패턴)까지 함께 관리.
- 에러: `PET_404`(petId 없음/본인 아님), `PET_400_3`(존재하지 않는 codeId 포함)

---

## 신규 예외 코드 정리
- `domain/mypage/exception/code/MypageErrorCode`: `MYPAGE_400_1`(알림설정 유효성), `MYPAGE_400_2`(이미 탈퇴), `MYPAGE_404_1`(문의 없음)
- `domain/mypage/exception/code/MypageSuccessCode`: 각 API별 200 코드
- `PetErrorCode`에 추가: `PET_REGISTRATION_404`, `PET_BLOOD_TYPE_MISMATCH`(PET_400_3), `PET_CODE_NOT_FOUND`(PET_400_4)
- `PetSuccessCode`에 추가: 목록/단건/전환/삭제/등록증/코드 관련 200 코드

## DB 마이그레이션 대상 (신규 테이블/컬럼)
- `users`: `is_deleted`, `deleted_at`
- `pet`: `blood_type`
- 신규 테이블: `notification_setting`, `notice`, `inquiry`, `pet_registration`, `ingredient_code`, `surgery_code`, `care_area_code`, `pet_excluded_ingredient`, `pet_surgery_history`, `pet_care_area`

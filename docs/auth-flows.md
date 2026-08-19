# 인증(로그인) 플로우 정리

> 기준 코드: `AuthController`, `AuthService`  
> 카카오 이메일이 있으면 로그인 시점에 로컬 계정과 연동 챌린지를 띄운다.  
> 카카오 온보딩은 이름·닉네임·한줄소개만 받으며, 로컬은 `/auth/signup/local` 회원가입이 별도이다.

---

## 1. API 목록

| Method | Path | 인증 | 설명 |
|--------|------|------|------|
| POST | `/auth/email/send` | X | 로컬 가입용 이메일 인증번호 전송 |
| POST | `/auth/email/verify` | X | 이메일 인증번호 확인 |
| POST | `/auth/signup/local` | X | 로컬 회원가입 (이메일 인증 필수) |
| POST | `/auth/login/local` | X | 로컬 로그인 |
| POST | `/auth/login/kakao` | X | 카카오 로그인 |
| POST | `/auth/onboarding` | JWT | 카카오 온보딩 (name, nickname, intro) |
| POST | `/auth/link/kakao` | JWT | 로그인 상태에서 카카오 연동 |
| POST | `/auth/link/confirm/kakao` | X | 연동 확인 (카카오로 본인 확인 → LOCAL 추가) |
| POST | `/auth/link/confirm/local` | X | 연동 확인 (로컬 비밀번호 → 카카오 계정 병합) |
| POST | `/auth/reissue` | X | Access/Refresh 재발급 |
| POST | `/auth/logout` | X | 로그아웃 (RefreshToken Redis 삭제) |

### Security

- **permitAll**: `/auth/signup/**`, `/auth/login/**`, `/auth/email/**`, `/auth/reissue`, `/auth/logout`, `/auth/link/confirm/**`
- **authenticated**: `/auth/onboarding`, `/auth/link/kakao`

---

## 2. 공통 응답

### LoginRes (로그인/연동 성공)

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "isNew": true,
  "uid": "uuid"
}
```

### LoginLinkChallengeRes (연동 창 필요, code=`LOGIN_LINK_201`)

```json
{
  "linkToken": "uuid",
  "existingProvider": "LOCAL | KAKAO",
  "email": "user@example.com"
}
```

- `linkToken`: Redis에 10분 보관
- `existingProvider`: 연동 창에서 확인해야 할 기존 로그인 수단

### RefreshToken (Redis)

- 키: `token_redis:{uid}:{provider}`
- provider: `LOCAL` | `KAKAO`

---

## 3. 로컬 회원가입 (이메일 인증 필요)

1. `POST /auth/email/send` `{ "email" }` → 6자리 코드 메일 발송 (Redis TTL 5분, 재전송 쿨다운 60초)
   - LOCAL 이미 있음 / 연동 불가 User → `LOCAL_SIGNUP_409_1`
   - **KAKAO만 있는 User는 연동 진입을 위해 발송 허용** (이후 signup/local → `LOGIN_LINK_201`)
2. `POST /auth/email/verify` `{ "email", "code" }` → 인증 완료 플래그 (TTL 30분)
3. `POST /auth/signup/local` `{ "email", "password" }`

```
이메일 미인증? → EMAIL_NOT_VERIFIED

email로 LOCAL OAuth 존재?
  └─ Y → LOCAL_SIGNUP_409_1

User.email 존재?
  ├─ Y + KAKAO만 있음(LOCAL 없음)
  │     → LOGIN_LINK_201 (existingProvider=KAKAO)
  │     → 프론트: 카카오 확인 후 /auth/link/confirm/kakao
  ├─ Y + 그 외 → LOCAL_SIGNUP_409_1
  └─ N → User(email, isNew=true) + OAuth(LOCAL, providerId=email, BCrypt password)
        → 인증 플래그 소비 → LoginRes (LOCAL_SIGNUP_200_1)
```

**비밀번호**: `BCrypt` (`PasswordEncoder`), `OAuth.providerId` = email

---

## 4. 로컬 로그인

`POST /auth/login/local`  
Body: `{ "email", "password" }`

```
LOCAL OAuth 존재?
  ├─ N
  │   User.email 있고 KAKAO만?
  │     ├─ Y → LOGIN_LINK_201 (existingProvider=KAKAO)
  │     └─ N → LOCAL_LOGIN_401_2
  └─ Y
      password 일치?
        ├─ N → LOCAL_LOGIN_401_2
        └─ Y → LoginRes
              isNew? LOCAL_LOGIN_200_1 : LOCAL_LOGIN_200_2
```

---

## 5. 카카오 로그인

`POST /auth/login/kakao`  
Body: `{ "accessToken" }` (카카오 SDK에서 받은 토큰)

```
카카오 /v2/user/me → providerId, email

OAuth(providerId, KAKAO) 존재?
  ├─ Y → LoginRes
  │       isNew? KAKAO_LOGIN_200_1 : KAKAO_LOGIN_200_2
  └─ N
      카카오 email이 있고 User.email 존재?
        ├─ 대상에 이미 KAKAO → LOGIN_LINK_400_3
        ├─ 대상에 LOCAL → LOGIN_LINK_201 (existingProvider=LOCAL)
        │                 → /auth/link/confirm/local (NEED_LOCAL_CONFIRM)
        ├─ 그 외 → LOCAL_SIGNUP_409_1
        └─ email 없거나 미사용
              → User(email=카카오email|null, isNew=true) + OAuth(KAKAO)
              → LoginRes (KAKAO_LOGIN_200_1)
```

- 카카오 이메일이 있으면 User.email에 저장
- 동일 이메일의 로컬 계정이 있으면 로그인 시점에 연동 창

---

## 6. 카카오 온보딩 (프로필)

`POST /auth/onboarding` (JWT 필요)  
Body: `{ "name", "nickname", "intro" }`

로컬은 `/auth/signup/local` 회원가입이 별도이며, 이 API는 카카오 신규(`isNew=true`) 프로필 입력용이다.

```
isNew == false? → ONBOARDING_400
그 외 → name, nickname, intro 저장 + isNew=false
       → ONBOARDING_200
```

`isNew=false`로 온보딩을 완료한다.

---

## 7. 연동 확인 플로우

### 7-1. 카카오로 확인 → LOCAL 붙이기

`POST /auth/link/confirm/kakao`  
Body: `{ "linkToken", "accessToken" }`

- pending 타입: `NEED_KAKAO_CONFIRM`
- 카카오 accessToken으로 본인 확인 (pending User와 동일)
- 해당 User에 `OAuth(LOCAL)` 추가
- 성공: `LOGIN_LINK_200` + LoginRes

**발생 경로**: 카카오 전용 계정(이미 이메일 있음)에 로컬 가입/로그인 시도

### 7-2. 로컬 비밀번호로 확인 → 카카오 연동

`POST /auth/link/confirm/local`  
Body: `{ "linkToken", "password" }`

**A) `NEED_LOCAL_CONFIRM`** (카카오 로그인 시 이메일 충돌)

- 로컬 비밀번호로 User 검증
- 해당 User에 `OAuth(KAKAO)` 추가 (신규 연동, User 병합 없음)
- 성공: `LOGIN_LINK_200` + LoginRes

**B) `NEED_LOCAL_CONFIRM_MERGE`** (온보딩 이메일 연동)

- 로컬 비밀번호로 target User 검증
- source의 KAKAO OAuth를 target으로 이전
- source User 삭제, Redis RT 삭제
- 성공: `LOGIN_LINK_200` + LoginRes

---

## 8. 로그인 상태에서 카카오 연동

`POST /auth/link/kakao` (JWT 필요)  
Body: `{ "accessToken" }`

```
현재 User에 이미 KAKAO?
  └─ Y → LOGIN_LINK_400_3

해당 kakao providerId가 다른 User에 있음?
  └─ Y → LOGIN_LINK_400

아니면 → 현재 User에 OAuth(KAKAO) 추가 (카카오 email 있으면 저장)
       → LOGIN_LINK_200 + LoginRes
```

**용도**: 로컬로 가입한 사용자가 설정/온보딩에서 카카오를 붙일 때

---

## 9. 토큰 재발급 / 로그아웃

### 재발급 `POST /auth/reissue`

Body: `{ "refreshToken" }`

1. JWT 유효 + typ=REFRESH
2. Redis 저장값과 일치
3. 새 access/refresh 발급 후 Redis 교체 → `REFRESH_200`

### 로그아웃 `POST /auth/logout`

Body: `{ "refreshToken" }`

1. Refresh 검증 + Redis 일치
2. Redis에서 해당 키 삭제 → `LOGOUT_200`

---

## 10. 시나리오별 프론트 가이드

### A. 로컬만 쓰는 신규

1. `/auth/signup/local` → 토큰 저장
2. `isNew=true`면 온보딩 (이메일은 이미 있음)

### B. 카카오 신규 (이메일 있음, 로컬과 미중복)

1. `/auth/login/kakao` → 토큰, email 저장, `isNew=true`
2. `/auth/onboarding` `{ name, nickname, intro }` → `isNew=false`

### C. 카카오 로그인인데 같은 이메일의 로컬 계정 있음

1. `/auth/login/kakao` → `LOGIN_LINK_201` (LOCAL)
2. 연동 창에서 로컬 비밀번호 입력
3. `/auth/link/confirm/local`

### D. 로컬 계정에 카카오 붙이기

1. 로컬 로그인
2. `/auth/link/kakao` + JWT + 카카오 accessToken

### E. 카카오 계정(이메일 있음)에 로컬 비밀번호 붙이기

1. `/auth/signup/local` 또는 `/auth/login/local` → `LOGIN_LINK_201` (KAKAO)
2. 연동 창에서 카카오 로그인
3. `/auth/link/confirm/kakao`

---

## 11. 데이터 모델 요약

| 테이블/저장소 | 역할 |
|---------------|------|
| `users` | uid, name, nickname, intro, email(**unique**), isNew |
| `oauth` | user, providerId+social_type(**unique**), password(LOCAL), email(**중복 허용**) |
| Redis `token_redis:{uid}:{provider}` | RefreshToken |
| Redis `auth:link:{linkToken}` | 연동 챌린지 pending (TTL 10분) |

### OAuth 규칙

| social_type | providerId | password |
|-------------|------------|----------|
| LOCAL | email | BCrypt 해시 |
| KAKAO | 카카오 회원 id | null |

---

## 12. 주요 응답 코드

| code | 의미 |
|------|------|
| `LOCAL_SIGNUP_200_1` | 로컬 회원가입 성공 |
| `LOCAL_LOGIN_200_1` / `200_2` | 로컬 로그인 (신규/기존) |
| `KAKAO_LOGIN_200_1` / `200_2` | 카카오 로그인 (신규/기존) |
| `ONBOARDING_200` | 카카오 온보딩 완료 |
| `LOGIN_LINK_201` | 연동 창 필요 |
| `LOGIN_LINK_200` | 연동 완료 |
| `REFRESH_200` | 토큰 재발급 |
| `LOGOUT_200` | 로그아웃 |
| `LOCAL_SIGNUP_409_1` | 이미 존재하는 아이디/이메일 |
| `LOCAL_LOGIN_401_2` | 아이디/비밀번호 오류 |
| `LOGIN_LINK_400` / `400_3` | 연동 실패 / 이미 연동됨 |
| `ONBOARDING_400` | 온보딩 실패 |

# FCM / Firebase Admin 시크릿

`google-services.json`은 Android 클라이언트 전용입니다. 깃에 커밋하지 마세요. 백엔드는 **Firebase Admin 서비스 계정** JSON을 `FCM_*` 환경 변수로 매핑해 사용합니다.

## 1. 서비스 계정 키 생성

1. Firebase Console → 프로젝트 `paw-fcm-ddfb3`
2. 프로젝트 설정 → 서비스 계정 → 새 비공개 키 생성
3. 다운로드한 `*-firebase-adminsdk-*.json`은 깃에 올리지 않음 (`.gitignore`에 포함됨)

## 2. JSON 필드 → env 매핑

| 서비스 계정 JSON | 환경 변수 |
|------------------|-----------|
| `type` | `FCM_TYPE` |
| `project_id` | `FCM_PROJECT_ID` |
| `private_key_id` | `FCM_PRIVATE_KEY_ID` |
| `private_key` | `FCM_PRIVATE_KEY` (한 줄; 실제 줄바꿈은 `\n`. **따옴표로 감싸지 않는 것을 권장** — `.env`를 properties로 읽을 때 따옴표가 값에 포함되어 `Invalid PKCS#8`가 납니다) |
| `client_email` | `FCM_CLIENT_EMAIL` |
| `client_id` | `FCM_CLIENT_ID` |
| `auth_uri` | `FCM_AUTH_URI` |
| `token_uri` | `FCM_TOKEN_URI` |
| `auth_provider_x509_cert_url` | `FCM_AUTH_PROVIDER_X509_CERT_URL` |
| `client_x509_cert_url` | `FCM_CLIENT_X509_CERT_URL` |
| `universe_domain` | `FCM_UNIVERSE_DOMAIN` |

`.env` / `EB_ENV_FILE` 예시:

```bash
FCM_TYPE=service_account
FCM_PROJECT_ID=paw-fcm-ddfb3
FCM_PRIVATE_KEY_ID=...
FCM_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n
FCM_CLIENT_EMAIL=...@paw-fcm-ddfb3.iam.gserviceaccount.com
FCM_CLIENT_ID=...
FCM_AUTH_URI=https://accounts.google.com/o/oauth2/auth
FCM_TOKEN_URI=https://oauth2.googleapis.com/token
FCM_AUTH_PROVIDER_X509_CERT_URL=https://www.googleapis.com/oauth2/v1/certs
FCM_CLIENT_X509_CERT_URL=https://www.googleapis.com/robot/v1/metadata/x509/...
FCM_UNIVERSE_DOMAIN=googleapis.com
```

## 3. 로컬 vs 배포

- **로컬:** `.env`에 변수를 넣습니다 (gitignore됨). Spring이 `application.yaml`을 통해 불러옵니다.
- **배포:** 같은 내용을 GitHub Actions 시크릿 `EB_ENV_FILE`에 추가합니다. 배포 워크플로가 `FCM_*`를 민감 키로 취급해 AWS Secrets Manager에 저장한 뒤 Elastic Beanstalk에 연결합니다.

이 백엔드에는 별도의 `GOOGLE_SERVICES_JSON` 시크릿을 만들지 않습니다.

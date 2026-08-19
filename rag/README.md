# RAG 지식 코퍼스 (로컬 적재 → dump → 수동 restore)

서버 앱은 청킹·임베딩·INSERT를 하지 않습니다. 로컬에서 ingest 프로필로 적재한 뒤 dump를 뽑아 운영 DB에 `pg_restore` 합니다.

`rag_document`는 전사문 요약 RAG용입니다. 약물 의미 검색은 별도 `medication` 테이블입니다.

## 디렉터리

| 경로 | 설명 |
|------|------|
| `data/` | 말뭉치 원본 (gitignore) |
| `sql/rag_document.sql` | RAG 테이블 DDL. 앱 기동과 무관 |
| `sql/medication.sql` | 약물 검색 마스터 DDL. 앱 기동과 무관 |
| `dumps/` | `pg_dump` 산출물 (gitignore) |

크롤 원본 SQL은 `src/main/resources/data/medicine.sql` (`medicine_dogcat`)입니다. 기동 시 자동 실행되지 않습니다.

## 말뭉치 로컬 적재

1. 말뭉치를 `rag/data/59.반려견 성장 및 질병 관련 말뭉치 데이터/`에 둡니다.
2. 로컬 Postgres에 `vector` 확장이 있는지 확인합니다.
3. 스키마 적용:

```bash
psql "$LOCAL_DB_URL" -f rag/sql/rag_document.sql
```

4. 시험 적재 (`limit=50`):

```bash
./gradlew bootRun --args='--spring.profiles.active=rag-ingest --paw.rag.limit=50'
```

5. 전체 적재:

```bash
./gradlew bootRun --args='--spring.profiles.active=rag-ingest'
```

재임베딩: `--paw.rag.force-reembed=true`  
과목 필터: `--paw.rag.department=내과`  
타입 필터: `--paw.rag.source-type=QA` (`QA` 또는 `CORPUS`)

`rag-ingest`는 `local` DB 설정을 함께 켭니다 (`spring.profiles.group`). 적재가 끝나면 프로세스가 종료됩니다. `OPENAI_API_KEY`와 로컬 DB 환경 변수가 필요합니다.

## 약물 로컬 적재

`item_seq`(크롤링 ID)가 `medication`에 이미 있으면 재작성·재임베딩 없이 건너뜁니다. 증분 크롤은 **신규 `item_seq`만** `medicine_dogcat`에 넣거나 `ON CONFLICT DO NOTHING`을 사용하세요.

1. 스키마 적용 (`vector` 확장 이후, 앱 기동 전에 권장):

```bash
psql "$LOCAL_DB_URL" -f rag/sql/medication.sql
```

2. 크롤 원본 적재:

```bash
psql "$LOCAL_DB_URL" -f src/main/resources/data/medicine.sql
```

3. 시험 적재 (`limit=5`):

```bash
./gradlew bootRun --args='--spring.profiles.active=medication-ingest --paw.medication.limit=5'
```

4. 전체 적재:

```bash
./gradlew bootRun --args='--spring.profiles.active=medication-ingest'
```

채팅/임베딩 모델은 `.env`에서 지정합니다. 비우지 말고 키를 빼면 YAML 기본값을 씁니다.

```
OPENAI_CHAT_MODEL=gpt-5.6-luna
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
# OPENAI_MEDICATION_CHAT_MODEL=gpt-5.6-luna
# OPENAI_MEDICATION_REASONING_EFFORT=none
```

약물 적재 채팅은 `OPENAI_MEDICATION_CHAT_MODEL`이 있으면 그걸, 없으면 `OPENAI_CHAT_MODEL`을 씁니다 (`reasoning.effort` 기본 `none`). 임베딩은 `text-embedding-3-small`(1536). `search_text`는 원본(이름/성분/효능/용법/주의)과 정제 마크다운을 이어붙인 스냅샷이며 API에는 내려가지 않습니다.

`medication-ingest`도 `local` 그룹입니다. 적재가 끝나면 프로세스가 종료됩니다.

## dump / restore

앱은 dump를 만들지 않습니다.

로컬 (적재 후):

```bash
pg_dump --format=custom --table=rag_document -f rag/dumps/rag_document.dump
pg_dump --format=custom --table=medication -f rag/dumps/medication.dump
```

운영 (수동):

```bash
# vector 확장은 인스턴스에 이미 있다고 가정
pg_restore --no-owner --exit-on-error -d "$PROD_DB_URL" rag/dumps/rag_document.dump
pg_restore --no-owner --exit-on-error -d "$PROD_DB_URL" rag/dumps/medication.dump
```

custom dump에 테이블·HNSW·데이터가 포함되므로 운영에서 DDL SQL을 따로 실행할 필요는 없습니다.

## OpenAI Vector Store (file_search)

`GET /rag?q=` 는 OpenAI 모델이 `file_search`로 벡터 저장소를 직접 조회한 뒤 답합니다. 앱이 임베딩하거나 pgvector를 검색하지 않습니다.

```bash
python3 rag/scripts/upload_openai_vector_store.py
```

`.env`에 `OPENAI_VECTOR_STORE_ID`를 넣습니다. 저장소 이름은 `paw-rag-knowledge`입니다. 산출물(`rag/openai-export/`)은 gitignore입니다.

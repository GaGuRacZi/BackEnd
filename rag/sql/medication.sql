-- Local/manual only. Do not hook this file to spring.sql.init or Hibernate.
-- Apply once against a DB that already has: CREATE EXTENSION IF NOT EXISTS vector;
-- Keep separate from rag_document so visit prescriptions can FK to medication(medication_id).

CREATE TABLE IF NOT EXISTS medication (
  medication_id   BIGSERIAL PRIMARY KEY,
  item_seq        VARCHAR(20)  NOT NULL,
  name_ko         VARCHAR(200) NOT NULL,
  name_en         VARCHAR(200),
  ingredient      TEXT,
  target_animal   VARCHAR(200),
  description_md  TEXT         NOT NULL,
  precaution_md   TEXT         NOT NULL,
  search_text     TEXT         NOT NULL,
  embedding       vector(1536) NOT NULL,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- If Hibernate created the table first (no vector column), add it.
ALTER TABLE medication ADD COLUMN IF NOT EXISTS embedding vector(1536);

CREATE UNIQUE INDEX IF NOT EXISTS medication_item_seq_uidx
  ON medication (item_seq);

CREATE INDEX IF NOT EXISTS medication_embedding_hnsw
  ON medication USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS medication_name_ko_idx
  ON medication (name_ko);

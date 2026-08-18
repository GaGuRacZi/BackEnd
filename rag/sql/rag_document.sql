-- Local/manual only. Do not hook this file to spring.sql.init or Hibernate.
-- Apply once against a DB that already has: CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS rag_document (
  id            BIGSERIAL PRIMARY KEY,
  source_id     VARCHAR(80)  NOT NULL,
  chunk_index   INT          NOT NULL,
  source_type   VARCHAR(16)  NOT NULL,
  department    VARCHAR(32),
  life_cycle    VARCHAR(32),
  disease       VARCHAR(128),
  title         VARCHAR(512),
  content       TEXT         NOT NULL,
  content_hash  CHAR(64)     NOT NULL,
  embedding     vector(1536) NOT NULL,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  UNIQUE (source_id, chunk_index)
);

CREATE INDEX IF NOT EXISTS rag_document_embedding_hnsw
  ON rag_document USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS rag_document_meta_idx
  ON rag_document (source_type, department, life_cycle);

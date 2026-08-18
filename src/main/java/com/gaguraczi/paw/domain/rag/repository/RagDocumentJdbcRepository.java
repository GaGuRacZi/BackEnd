package com.gaguraczi.paw.domain.rag.repository;

import com.gaguraczi.paw.domain.rag.exception.code.RagErrorCode;
import com.gaguraczi.paw.domain.rag.model.RagChunk;
import com.gaguraczi.paw.domain.rag.support.PgVectorLiteral;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RagDocumentJdbcRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO rag_document (
                source_id, chunk_index, source_type, department, life_cycle, disease, title,
                content, content_hash, embedding, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::vector, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (source_id, chunk_index) DO UPDATE SET
                source_type = EXCLUDED.source_type,
                department = EXCLUDED.department,
                life_cycle = EXCLUDED.life_cycle,
                disease = EXCLUDED.disease,
                title = EXCLUDED.title,
                content = EXCLUDED.content,
                content_hash = EXCLUDED.content_hash,
                embedding = EXCLUDED.embedding,
                updated_at = CURRENT_TIMESTAMP
            """;

    private static final String HASH_SQL_PREFIX = """
            SELECT source_id, chunk_index, content_hash
            FROM rag_document
            WHERE source_id IN (
            """;

    private final JdbcTemplate jdbcTemplate;

    public Map<String, String> findHashes(List<String> sourceIds) {
        if (sourceIds == null || sourceIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = String.join(",", sourceIds.stream().map(id -> "?").toList());
        String sql = HASH_SQL_PREFIX + placeholders + ")";
        try {
            return jdbcTemplate.query(sql, rs -> {
                Map<String, String> hashes = new HashMap<>();
                while (rs.next()) {
                    String hash = rs.getString("content_hash");
                    hashes.put(
                            rs.getString("source_id") + ":" + rs.getInt("chunk_index"),
                            hash == null ? null : hash.trim()
                    );
                }
                return hashes;
            }, sourceIds.toArray());
        } catch (DataAccessException e) {
            throw wrapTableMissing(e);
        }
    }

    public void upsert(RagChunk chunk, String contentHash, float[] embedding) {
        try {
            jdbcTemplate.update(
                    UPSERT_SQL,
                    chunk.sourceId(),
                    chunk.chunkIndex(),
                    chunk.sourceType().name(),
                    chunk.department(),
                    chunk.lifeCycle(),
                    chunk.disease(),
                    chunk.title(),
                    chunk.content(),
                    contentHash,
                    PgVectorLiteral.of(embedding)
            );
        } catch (DataAccessException e) {
            throw wrapTableMissing(e);
        }
    }

    private static RuntimeException wrapTableMissing(DataAccessException e) {
        if (isUndefinedTable(e)) {
            return GeneralException.of(RagErrorCode.RAG_TABLE_MISSING, e);
        }
        return e;
    }

    private static boolean isUndefinedTable(DataAccessException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof SQLException sqlException && "42P01".equals(sqlException.getSQLState())) {
            return true;
        }
        String message = cause != null ? cause.getMessage() : e.getMessage();
        return message != null
                && message.contains("rag_document")
                && message.toLowerCase().contains("does not exist");
    }
}

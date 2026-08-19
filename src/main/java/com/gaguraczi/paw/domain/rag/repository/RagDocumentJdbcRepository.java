package com.gaguraczi.paw.domain.rag.repository;

import com.gaguraczi.paw.domain.rag.exception.code.RagErrorCode;
import com.gaguraczi.paw.domain.rag.model.RagChunk;
import com.gaguraczi.paw.domain.rag.support.PgVectorLiteral;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        upsertAll(List.of(chunk), List.of(contentHash), List.of(embedding));
    }

    public void upsertAll(List<RagChunk> chunks, List<String> hashes, List<float[]> embeddings) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        try {
            jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    RagChunk chunk = chunks.get(i);
                    ps.setString(1, chunk.sourceId());
                    ps.setInt(2, chunk.chunkIndex());
                    ps.setString(3, chunk.sourceType().name());
                    ps.setString(4, chunk.department());
                    ps.setString(5, chunk.lifeCycle());
                    ps.setString(6, chunk.disease());
                    ps.setString(7, chunk.title());
                    ps.setString(8, chunk.content());
                    ps.setString(9, hashes.get(i));
                    ps.setString(10, PgVectorLiteral.of(embeddings.get(i)));
                }

                @Override
                public int getBatchSize() {
                    return chunks.size();
                }
            });
        } catch (DataAccessException e) {
            throw wrapTableMissing(e);
        }
    }

    private static RuntimeException wrapTableMissing(DataAccessException e) {
        if (isUndefinedTable(e)) {
            log.warn("rag_document table is missing. Apply rag/sql/rag_document.sql or restore a dump.", e);
            return GeneralException.of(RagErrorCode.RAG_TABLE_MISSING, e);
        }
        return e;
    }

    private static boolean isUndefinedTable(DataAccessException e) {
        if (hasUndefinedTableState(e.getMostSpecificCause())) {
            return true;
        }
        return e instanceof BadSqlGrammarException && hasUndefinedTableState(e.getCause());
    }

    private static boolean hasUndefinedTableState(Throwable cause) {
        return cause instanceof SQLException sqlException && "42P01".equals(sqlException.getSQLState());
    }
}

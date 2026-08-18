package com.gaguraczi.paw.domain.medication.repository;

import com.gaguraczi.paw.domain.medication.dto.MedicationSearchHit;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.model.MedicineStagingRow;
import com.gaguraczi.paw.domain.rag.support.PgVectorLiteral;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MedicationJdbcRepository {

    private static final String PENDING_SQL = """
            SELECT item_seq, product_name, product_name_en, ingredients, efficacy, dosage, precaution, target_animal
            FROM medicine_dogcat s
            WHERE NOT EXISTS (
                SELECT 1 FROM medication m WHERE m.item_seq = s.item_seq
            )
            ORDER BY s.item_seq
            """;

    private static final String PENDING_SQL_LIMIT = PENDING_SQL + " LIMIT ?";

    private static final String INSERT_SQL = """
            INSERT INTO medication (
                item_seq, name_ko, name_en, ingredient, target_animal,
                description_md, precaution_md, search_text, embedding, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::vector, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (item_seq) DO NOTHING
            """;

    private static final String VECTOR_SEARCH_SQL = """
            SELECT medication_id, name_ko, name_en, ingredient,
                   1 - (embedding <=> ?::vector) AS score
            FROM medication
            ORDER BY embedding <=> ?::vector
            LIMIT ?
            """;

    private static final String LEXICAL_SEARCH_SQL = """
            SELECT medication_id, name_ko, name_en, ingredient, 1.0 AS score
            FROM medication
            WHERE name_ko ILIKE ? ESCAPE E'\\\\'
               OR COALESCE(name_en, '') ILIKE ? ESCAPE E'\\\\'
               OR COALESCE(ingredient, '') ILIKE ? ESCAPE E'\\\\'
            ORDER BY name_ko
            LIMIT ?
            """;

    private static final RowMapper<MedicineStagingRow> STAGING_MAPPER = MedicationJdbcRepository::mapStaging;
    private static final RowMapper<MedicationSearchHit> VECTOR_MAPPER =
            (rs, rowNum) -> mapHit(rs, false);
    private static final RowMapper<MedicationSearchHit> LEXICAL_MAPPER =
            (rs, rowNum) -> mapHit(rs, true);

    private final JdbcTemplate jdbcTemplate;

    public List<MedicineStagingRow> findPending(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                return jdbcTemplate.query(PENDING_SQL, STAGING_MAPPER);
            }
            return jdbcTemplate.query(PENDING_SQL_LIMIT, STAGING_MAPPER, limit);
        } catch (DataAccessException e) {
            throw wrapTableMissing(e);
        }
    }

    public void insert(
            MedicineStagingRow row,
            String descriptionMd,
            String precautionMd,
            String searchText,
            float[] embedding
    ) {
        try {
            jdbcTemplate.update(
                    INSERT_SQL,
                    row.itemSeq(),
                    row.productName(),
                    blankToNull(row.productNameEn()),
                    blankToNull(row.ingredients()),
                    blankToNull(row.targetAnimal()),
                    descriptionMd,
                    precautionMd,
                    searchText,
                    PgVectorLiteral.of(embedding)
            );
        } catch (DataAccessException e) {
            throw wrapTableMissing(e);
        }
    }

    public List<MedicationSearchHit> searchVector(float[] queryEmbedding, int topK) {
        String vector = PgVectorLiteral.of(queryEmbedding);
        try {
            return jdbcTemplate.query(VECTOR_SEARCH_SQL, VECTOR_MAPPER, vector, vector, topK);
        } catch (DataAccessException e) {
            throw wrapTableMissing(e);
        }
    }

    public List<MedicationSearchHit> searchLexical(String likePattern, int limit) {
        try {
            return jdbcTemplate.query(
                    LEXICAL_SEARCH_SQL,
                    LEXICAL_MAPPER,
                    likePattern,
                    likePattern,
                    likePattern,
                    limit
            );
        } catch (DataAccessException e) {
            throw wrapTableMissing(e);
        }
    }

    private static MedicineStagingRow mapStaging(ResultSet rs, int rowNum) throws SQLException {
        return new MedicineStagingRow(
                rs.getString("item_seq"),
                rs.getString("product_name"),
                rs.getString("product_name_en"),
                rs.getString("ingredients"),
                rs.getString("efficacy"),
                rs.getString("dosage"),
                rs.getString("precaution"),
                rs.getString("target_animal")
        );
    }

    private static MedicationSearchHit mapHit(ResultSet rs, boolean lexicalMatch) throws SQLException {
        return new MedicationSearchHit(
                rs.getLong("medication_id"),
                rs.getString("name_ko"),
                rs.getString("name_en"),
                rs.getString("ingredient"),
                rs.getDouble("score"),
                lexicalMatch
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static RuntimeException wrapTableMissing(DataAccessException e) {
        if (isUndefinedTable(e, "medicine_dogcat")) {
            return GeneralException.of(MedicationErrorCode.MEDICATION_STAGING_MISSING, e);
        }
        if (isUndefinedTable(e, "medication")) {
            return GeneralException.of(MedicationErrorCode.MEDICATION_TABLE_MISSING, e);
        }
        return e;
    }

    private static boolean isUndefinedTable(DataAccessException e, String table) {
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof SQLException sqlException && "42P01".equals(sqlException.getSQLState())) {
            String message = sqlException.getMessage();
            return message != null && message.contains(table);
        }
        String message = cause != null ? cause.getMessage() : e.getMessage();
        return message != null
                && message.contains(table)
                && message.toLowerCase().contains("does not exist");
    }
}

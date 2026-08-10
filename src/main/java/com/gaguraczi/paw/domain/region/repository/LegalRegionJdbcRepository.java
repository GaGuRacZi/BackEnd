package com.gaguraczi.paw.domain.region.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LegalRegionJdbcRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO legal_region (code, name, level, parent_code, abolished, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (code) DO UPDATE SET
                name = EXCLUDED.name,
                level = EXCLUDED.level,
                parent_code = EXCLUDED.parent_code,
                abolished = EXCLUDED.abolished,
                updated_at = CURRENT_TIMESTAMP
            """;

    private final JdbcTemplate jdbcTemplate;

    public void batchUpsert(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(UPSERT_SQL, rows);
    }
}

package com.gaguraczi.paw.domain.breed.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BreedJdbcRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO breed (pet_type, name, is_popular, created_at, updated_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (pet_type, name) DO UPDATE SET
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

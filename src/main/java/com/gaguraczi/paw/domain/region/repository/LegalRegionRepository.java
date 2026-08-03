package com.gaguraczi.paw.domain.region.repository;

import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.region.enums.RegionLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LegalRegionRepository extends JpaRepository<LegalRegion, String> {

    Optional<LegalRegion> findByCodeAndAbolishedFalseAndLevel(String code, RegionLevel level);

    @Query("""
            SELECT r FROM LegalRegion r
            WHERE r.abolished = false
              AND r.level = com.gaguraczi.paw.domain.region.enums.RegionLevel.SIGUNGU
              AND (
                    LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR EXISTS (
                      SELECT 1 FROM LegalRegion d
                      WHERE d.abolished = false
                        AND d.level = com.gaguraczi.paw.domain.region.enums.RegionLevel.DONG
                        AND d.parentCode = r.code
                        AND LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%'))
                 )
              )
            ORDER BY r.name ASC
            """)
    List<LegalRegion> searchActiveSigungu(@Param("q") String q, Pageable pageable);

    List<LegalRegion> findTop20ByParentCodeAndLevelAndAbolishedFalseOrderByNameAsc(
            String parentCode,
            RegionLevel level
    );

    List<LegalRegion> findByParentCodeInAndLevelAndAbolishedFalseOrderByNameAsc(
            Collection<String> parentCodes,
            RegionLevel level
    );

    long countByAbolishedFalse();
}

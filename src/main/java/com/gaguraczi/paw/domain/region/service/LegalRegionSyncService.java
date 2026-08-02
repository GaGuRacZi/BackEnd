package com.gaguraczi.paw.domain.region.service;

import com.gaguraczi.paw.domain.region.enums.RegionLevel;
import com.gaguraczi.paw.domain.region.repository.LegalRegionJdbcRepository;
import com.gaguraczi.paw.domain.region.repository.LegalRegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegalRegionSyncService {

    private static final int BATCH_SIZE = 500;

    private final LegalRegionJdbcRepository legalRegionJdbcRepository;
    private final LegalRegionRepository legalRegionRepository;

    public record SyncResult(int processed, long totalAfter) {}

    @Transactional
    public SyncResult syncFromClasspath() {
        ClassPathResource resource = new ClassPathResource("data/legal-dong.txt");
        if (!resource.exists()) {
            throw new IllegalStateException("법정동 데이터 파일이 없습니다: classpath:data/legal-dong.txt");
        }

        int processed = 0;
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (!headerSkipped) {
                    headerSkipped = true;
                    if (line.startsWith("법정동코드")) {
                        continue;
                    }
                }

                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }

                String code = parts[0].trim();
                String name = parts[1].trim();
                String status = parts[2].trim();
                if (code.length() != 10) {
                    continue;
                }

                RegionLevel level = RegionLevel.fromCode(code);
                String parentCode = RegionLevel.parentCodeOf(code, level);
                boolean abolished = "폐지".equals(status);

                batch.add(new Object[]{code, name, level.name(), parentCode, abolished});
                if (batch.size() >= BATCH_SIZE) {
                    legalRegionJdbcRepository.batchUpsert(batch);
                    processed += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                legalRegionJdbcRepository.batchUpsert(batch);
                processed += batch.size();
            }
        } catch (Exception e) {
            throw new IllegalStateException("법정동 동기화에 실패했습니다.", e);

        }

        long total = legalRegionRepository.count();
        log.info("LegalRegion sync finished. processed={}, total={}", processed, total);
        return new SyncResult(processed, total);
    }

    public boolean isEmpty() {
        return legalRegionRepository.count() == 0;
    }
}

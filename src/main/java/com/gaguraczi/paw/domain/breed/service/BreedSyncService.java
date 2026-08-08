package com.gaguraczi.paw.domain.breed.service;

import com.gaguraczi.paw.domain.breed.repository.BreedJdbcRepository;
import com.gaguraczi.paw.domain.breed.repository.BreedRepository;
import com.gaguraczi.paw.domain.users.enums.PetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
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
public class BreedSyncService {

    private static final int BATCH_SIZE = 200;
    private static final String DOG_FILE = "data/breed-dog.txt";
    private static final String CAT_FILE = "data/breed-cat.txt";

    private final BreedJdbcRepository breedJdbcRepository;
    private final BreedRepository breedRepository;

    public record SyncResult(int dogProcessed, int catProcessed, long totalAfter) {}

    @Transactional
    public SyncResult syncFromClasspath() {
        int dogProcessed = syncFile(DOG_FILE, PetType.DOG);
        int catProcessed = syncFile(CAT_FILE, PetType.CAT);
        long total = breedRepository.count();
        log.info("Breed sync finished. dog={}, cat={}, total={}", dogProcessed, catProcessed, total);
        return new SyncResult(dogProcessed, catProcessed, total);
    }

    public boolean isEmpty() {
        return breedRepository.count() == 0;
    }

    private int syncFile(String classpath, PetType petType) {
        ClassPathResource resource = new ClassPathResource(classpath);
        if (!resource.exists()) {
            throw new IllegalStateException("품종 데이터 파일이 없습니다: classpath:" + classpath);
        }

        int processed = 0;
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String name = line.replace("\uFEFF", "").trim();
                if (name.isEmpty()) {
                    continue;
                }
                batch.add(new Object[]{petType.name(), name, false});
                if (batch.size() >= BATCH_SIZE) {
                    breedJdbcRepository.batchUpsert(batch);
                    processed += batch.size();
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                breedJdbcRepository.batchUpsert(batch);
                processed += batch.size();
            }
        } catch (Exception e) {
            throw new IllegalStateException("품종 동기화에 실패했습니다: " + classpath, e);
        }

        return processed;
    }
}

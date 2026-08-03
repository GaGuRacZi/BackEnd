package com.gaguraczi.paw.domain.breed.config;

import com.gaguraczi.paw.domain.breed.service.BreedSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(60)
@RequiredArgsConstructor
public class BreedDataLoader implements ApplicationRunner {

    private final BreedSyncService breedSyncService;

    @Override
    public void run(ApplicationArguments args) {
        if (!breedSyncService.isEmpty()) {
            log.info("Breed already loaded. skip initial import.");
            return;
        }
        log.info("Breed table empty. starting initial sync...");
        BreedSyncService.SyncResult result = breedSyncService.syncFromClasspath();
        log.info("Breed initial sync done: dog={}, cat={}, total={}",
                result.dogProcessed(), result.catProcessed(), result.totalAfter());
    }
}

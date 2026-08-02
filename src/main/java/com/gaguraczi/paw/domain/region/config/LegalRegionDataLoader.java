package com.gaguraczi.paw.domain.region.config;

import com.gaguraczi.paw.domain.region.service.LegalRegionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class LegalRegionDataLoader implements ApplicationRunner {

    private final LegalRegionSyncService legalRegionSyncService;

    @Override
    public void run(ApplicationArguments args) {
        if (!legalRegionSyncService.isEmpty()) {
            log.info("LegalRegion already loaded. skip initial import.");
            return;
        }
        log.info("LegalRegion table empty. starting initial sync...");
        LegalRegionSyncService.SyncResult result = legalRegionSyncService.syncFromClasspath();
        log.info("LegalRegion initial sync done: {}", result);
    }
}

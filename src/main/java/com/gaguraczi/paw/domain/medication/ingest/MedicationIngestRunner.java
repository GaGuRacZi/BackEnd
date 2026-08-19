package com.gaguraczi.paw.domain.medication.ingest;

import com.gaguraczi.paw.domain.medication.service.MedicationIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("medication-ingest")
@Order(200)
@RequiredArgsConstructor
public class MedicationIngestRunner implements ApplicationRunner {

    private final MedicationIngestService medicationIngestService;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        MedicationIngestService.IngestResult result = medicationIngestService.ingest();
        log.info(
                "medication ingest finished. processed={} skipped={} failed={} pending={}",
                result.processed(),
                result.skipped(),
                result.failed(),
                result.pending()
        );
        log.info("Dump command: pg_dump --format=custom --table=medication -f rag/dumps/medication.dump");
        int code = result.failed() > 0 ? 1 : 0;
        System.exit(SpringApplication.exit(context, () -> code));
    }
}

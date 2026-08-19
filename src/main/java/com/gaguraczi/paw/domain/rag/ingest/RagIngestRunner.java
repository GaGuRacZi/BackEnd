package com.gaguraczi.paw.domain.rag.ingest;

import com.gaguraczi.paw.domain.rag.service.RagIngestService;
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
@Profile("rag-ingest")
@Order(200)
@RequiredArgsConstructor
public class RagIngestRunner implements ApplicationRunner {

    private final RagIngestService ragIngestService;
    private final ConfigurableApplicationContext context;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        RagIngestService.IngestResult result = ragIngestService.ingest();
        log.info(
                "RAG ingest finished. processed={} skipped={} failed={} files={}",
                result.processed(),
                result.skipped(),
                result.failed(),
                result.files()
        );
        log.info("Dump command: pg_dump --format=custom --table=rag_document -f rag/dumps/rag_document.dump");
        int code = result.failed() > 0 ? 1 : 0;
        System.exit(SpringApplication.exit(context, () -> code));
    }
}

package com.gaguraczi.paw.domain.medication.service;

import com.gaguraczi.paw.domain.medication.config.MedicationProperties;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.model.MedicationCopy;
import com.gaguraczi.paw.domain.medication.model.MedicineStagingRow;
import com.gaguraczi.paw.domain.medication.repository.MedicationJdbcRepository;
import com.gaguraczi.paw.domain.medication.support.MedicationSearchText;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("medication-ingest")
@RequiredArgsConstructor
public class MedicationIngestService {

    private final MedicationProperties medicationProperties;
    private final MedicationJdbcRepository medicationJdbcRepository;
    private final MedicationCopyRewriter medicationCopyRewriter;
    private final EmbeddingModel embeddingModel;

    public IngestResult ingest() {
        List<MedicineStagingRow> pending = medicationJdbcRepository.findPending(medicationProperties.getLimit());
        int batchSize = Math.max(1, medicationProperties.getIngestBatchSize());
        int processed = 0;
        int skipped = 0;
        int failed = 0;

        List<MedicineStagingRow> buffer = new ArrayList<>(batchSize);
        for (MedicineStagingRow row : pending) {
            if (row.productName() == null || row.productName().isBlank()) {
                skipped++;
                log.warn("medication ingest skipped blank name item_seq={}", row.itemSeq());
                continue;
            }
            buffer.add(row);
            if (buffer.size() >= batchSize) {
                FlushResult flush = flush(buffer);
                processed += flush.processed();
                skipped += flush.skipped();
                failed += flush.failed();
                buffer.clear();
                if (processed > 0 && processed % 32 == 0) {
                    log.info("medication ingest progress processed={} skipped={} failed={}",
                            processed, skipped, failed);
                }
            }
        }
        if (!buffer.isEmpty()) {
            FlushResult flush = flush(buffer);
            processed += flush.processed();
            skipped += flush.skipped();
            failed += flush.failed();
        }
        return new IngestResult(processed, skipped, failed, pending.size());
    }

    private FlushResult flush(List<MedicineStagingRow> rows) {
        List<PreparedRow> prepared = new ArrayList<>();
        int failed = 0;
        Map<String, MedicationCopy> copies = medicationCopyRewriter.rewriteBatch(rows);
        for (MedicineStagingRow row : rows) {
            MedicationCopy copy = copies.get(row.itemSeq());
            if (copy == null) {
                failed++;
                log.warn("medication rewrite skipped item_seq={} (batch miss)", row.itemSeq());
                continue;
            }
            String searchText = MedicationSearchText.of(row, copy.descriptionMd(), copy.precautionMd());
            prepared.add(new PreparedRow(row, copy, searchText));
        }
        if (prepared.isEmpty()) {
            return new FlushResult(0, 0, failed);
        }

        List<float[]> embeddings;
        try {
            embeddings = embeddingModel.embed(prepared.stream().map(PreparedRow::searchText).toList());
        } catch (RuntimeException e) {
            log.error("OpenAI embedding request failed: {}", rootMessage(e), e);
            throw GeneralException.of(MedicationErrorCode.MEDICATION_EMBEDDING_FAILED, e);
        }
        if (embeddings.size() != prepared.size()) {
            throw GeneralException.of(MedicationErrorCode.MEDICATION_EMBEDDING_FAILED);
        }

        int expectedDim = medicationProperties.getEmbeddingDimensions();
        int processed = 0;
        int skipped = 0;
        for (int i = 0; i < prepared.size(); i++) {
            float[] embedding = embeddings.get(i);
            if (embedding == null || embedding.length != expectedDim) {
                throw GeneralException.of(MedicationErrorCode.MEDICATION_EMBEDDING_FAILED);
            }
            PreparedRow item = prepared.get(i);
            int affected = medicationJdbcRepository.insert(
                    item.row(),
                    item.copy().descriptionMd(),
                    item.copy().precautionMd(),
                    item.searchText(),
                    embedding
            );
            if (affected == 0) {
                skipped++;
            } else {
                processed++;
            }
        }
        return new FlushResult(processed, skipped, failed);
    }

    private static String rootMessage(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : current.toString();
    }

    public record IngestResult(int processed, int skipped, int failed, int pending) {
    }

    private record FlushResult(int processed, int skipped, int failed) {
    }

    private record PreparedRow(MedicineStagingRow row, MedicationCopy copy, String searchText) {
    }
}

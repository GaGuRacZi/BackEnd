package com.gaguraczi.paw.domain.medication.service;

import com.gaguraczi.paw.domain.medication.config.MedicationProperties;
import com.gaguraczi.paw.domain.medication.model.MedicationCopy;
import com.gaguraczi.paw.domain.medication.model.MedicineStagingRow;
import com.gaguraczi.paw.domain.medication.repository.MedicationJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationIngestServiceTest {

    @Mock
    private MedicationJdbcRepository jdbcRepository;
    @Mock
    private MedicationCopyRewriter copyRewriter;
    @Mock
    private EmbeddingModel embeddingModel;

    private MedicationIngestService ingestService;

    @BeforeEach
    void setUp() {
        MedicationProperties properties = new MedicationProperties();
        properties.setIngestBatchSize(8);
        properties.setEmbeddingDimensions(2);
        ingestService = new MedicationIngestService(
                properties, jdbcRepository, copyRewriter, embeddingModel);
    }

    @Test
    void countsZeroAffectedInsertAsSkipped() {
        MedicineStagingRow row = stagingRow("100");
        stubSuccessfulPrepare(row);
        when(jdbcRepository.insert(any(), any(), any(), any(), any())).thenReturn(0);

        MedicationIngestService.IngestResult result = ingestService.ingest();

        assertThat(result.processed()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failed()).isZero();
    }

    @Test
    void countsSuccessfulInsertAsProcessed() {
        MedicineStagingRow row = stagingRow("101");
        stubSuccessfulPrepare(row);
        when(jdbcRepository.insert(any(), any(), any(), any(), any())).thenReturn(1);

        MedicationIngestService.IngestResult result = ingestService.ingest();

        assertThat(result.processed()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        assertThat(result.failed()).isZero();
    }

    private void stubSuccessfulPrepare(MedicineStagingRow row) {
        when(jdbcRepository.findPending(any())).thenReturn(List.of(row));
        when(copyRewriter.rewriteBatch(anyList())).thenReturn(Map.of(
                row.itemSeq(), new MedicationCopy("설명", "주의")));
        when(embeddingModel.embed(anyList())).thenReturn(List.of(new float[]{0.1f, 0.2f}));
    }

    private static MedicineStagingRow stagingRow(String itemSeq) {
        return new MedicineStagingRow(
                itemSeq, "카미녹스", null, null, null, null, null, null);
    }
}

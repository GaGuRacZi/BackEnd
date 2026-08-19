package com.gaguraczi.paw.domain.medication.service;

import com.gaguraczi.paw.domain.medication.config.MedicationProperties;
import com.gaguraczi.paw.domain.medication.dto.MedicationSearchHit;
import com.gaguraczi.paw.domain.medication.entity.Medication;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.repository.MedicationJdbcRepository;
import com.gaguraczi.paw.domain.medication.repository.MedicationRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationSearchServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;
    @Mock
    private MedicationJdbcRepository jdbcRepository;
    @Mock
    private MedicationRepository medicationRepository;

    private MedicationSearchService searchService;

    @BeforeEach
    void setUp() {
        MedicationProperties properties = new MedicationProperties();
        properties.setSearchTopK(10);
        properties.setEmbeddingDimensions(2);
        searchService = new MedicationSearchService(
                embeddingModel, jdbcRepository, medicationRepository, properties);
    }

    @Test
    void rejectsBlankQuery() {
        assertThatThrownBy(() -> searchService.search("  ", null))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MedicationErrorCode.MEDICATION_QUERY_REQUIRED);
    }

    @Test
    void embedsAndMergesLexicalFirst() {
        float[] vector = new float[]{0.1f, 0.2f};
        when(embeddingModel.embed("카미녹스")).thenReturn(vector);
        when(jdbcRepository.searchVector(vector, 5)).thenReturn(List.of(
                new MedicationSearchHit(2L, "리리카", null, "가바펜틴", 0.91, false)
        ));
        when(jdbcRepository.searchLexical("%카미녹스%", 5)).thenReturn(List.of(
                new MedicationSearchHit(1L, "카미녹스", "Carprofen", "카르프로펜", 1.0, true)
        ));

        List<MedicationSearchHit> hits = searchService.search("카미녹스", 5);

        assertThat(hits).extracting(MedicationSearchHit::medicationId).containsExactly(1L, 2L);
        verify(jdbcRepository).searchVector(eq(vector), eq(5));
        verify(jdbcRepository).searchLexical(eq("%카미녹스%"), eq(5));
    }

    @Test
    void usesDefaultTopK() {
        float[] vector = new float[]{0.1f, 0.2f};
        when(embeddingModel.embed("관절")).thenReturn(vector);
        when(jdbcRepository.searchVector(vector, 10)).thenReturn(List.of());
        when(jdbcRepository.searchLexical("%관절%", 10)).thenReturn(List.of());

        searchService.search("관절", null);

        verify(jdbcRepository).searchVector(vector, 10);
    }

    @Test
    void capsTopKAtConfiguredMaximum() {
        float[] vector = new float[]{0.1f, 0.2f};
        when(embeddingModel.embed("관절")).thenReturn(vector);
        when(jdbcRepository.searchVector(vector, 10)).thenReturn(List.of());
        when(jdbcRepository.searchLexical("%관절%", 10)).thenReturn(List.of());

        searchService.search("관절", 50);

        verify(jdbcRepository).searchVector(vector, 10);
        verify(jdbcRepository).searchLexical("%관절%", 10);
    }

    @Test
    void rejectsNullEmbedding() {
        when(embeddingModel.embed("카미녹스")).thenReturn(null);

        assertThatThrownBy(() -> searchService.search("카미녹스", 5))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MedicationErrorCode.MEDICATION_EMBEDDING_FAILED);
    }

    @Test
    void rejectsWrongEmbeddingDimension() {
        when(embeddingModel.embed("카미녹스")).thenReturn(new float[]{0.1f});

        assertThatThrownBy(() -> searchService.search("카미녹스", 5))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MedicationErrorCode.MEDICATION_EMBEDDING_FAILED);
    }

    @Test
    void escapesLikeWildcards() {
        assertThat(MedicationSearchService.toLikePattern("100%")).isEqualTo("%100\\%%");
        assertThat(MedicationSearchService.toLikePattern("a_b")).isEqualTo("%a\\_b%");
    }

    @Test
    void getThrowsWhenMissing() {
        when(medicationRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> searchService.get(9L))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MedicationErrorCode.MEDICATION_NOT_FOUND);
    }

    @Test
    void getReturnsEntity() {
        Medication medication = Medication.builder()
                .medicationId(1L)
                .itemSeq("2023")
                .nameKo("카미녹스")
                .descriptionMd("설명")
                .precautionMd("- 주의")
                .searchText("이름: 카미녹스")
                .build();
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(medication));

        assertThat(searchService.get(1L).getNameKo()).isEqualTo("카미녹스");
    }
}

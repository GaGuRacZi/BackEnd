package com.gaguraczi.paw.domain.medication.service;

import com.gaguraczi.paw.domain.medication.config.MedicationProperties;
import com.gaguraczi.paw.domain.medication.dto.MedicationSearchHit;
import com.gaguraczi.paw.domain.medication.entity.Medication;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.repository.MedicationJdbcRepository;
import com.gaguraczi.paw.domain.medication.repository.MedicationRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicationSearchService {

    private final EmbeddingModel embeddingModel;
    private final MedicationJdbcRepository medicationJdbcRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationProperties medicationProperties;

    public List<MedicationSearchHit> search(String query, Integer topK) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            throw GeneralException.of(MedicationErrorCode.MEDICATION_QUERY_REQUIRED);
        }
        int limit = topK == null || topK <= 0 ? medicationProperties.getSearchTopK() : topK;
        float[] embedding = embeddingModel.embed(trimmed);
        List<MedicationSearchHit> vectorHits = medicationJdbcRepository.searchVector(embedding, limit);
        List<MedicationSearchHit> lexicalHits =
                medicationJdbcRepository.searchLexical(toLikePattern(trimmed), limit);
        return merge(lexicalHits, vectorHits, limit);
    }

    public Medication get(Long medicationId) {
        return medicationRepository.findById(medicationId)
                .orElseThrow(() -> GeneralException.of(MedicationErrorCode.MEDICATION_NOT_FOUND));
    }

    static String toLikePattern(String query) {
        String escaped = query
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    static List<MedicationSearchHit> merge(
            List<MedicationSearchHit> lexicalHits,
            List<MedicationSearchHit> vectorHits,
            int limit
    ) {
        Map<Long, MedicationSearchHit> merged = new LinkedHashMap<>();
        for (MedicationSearchHit hit : lexicalHits) {
            merged.put(hit.medicationId(), hit);
        }
        for (MedicationSearchHit hit : vectorHits) {
            MedicationSearchHit existing = merged.get(hit.medicationId());
            if (existing == null) {
                merged.put(hit.medicationId(), hit);
            } else if (hit.score() > existing.score()) {
                merged.put(hit.medicationId(), new MedicationSearchHit(
                        existing.medicationId(),
                        existing.nameKo(),
                        existing.nameEn(),
                        existing.ingredient(),
                        hit.score(),
                        true
                ));
            }
        }
        return merged.values().stream()
                .sorted(Comparator
                        .comparing(MedicationSearchHit::lexicalMatch).reversed()
                        .thenComparing(Comparator.comparingDouble(MedicationSearchHit::score).reversed())
                        .thenComparing(MedicationSearchHit::nameKo, Comparator.nullsLast(String::compareTo)))
                .limit(limit)
                .toList();
    }
}

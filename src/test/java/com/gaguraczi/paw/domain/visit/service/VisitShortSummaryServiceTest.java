package com.gaguraczi.paw.domain.visit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class VisitShortSummaryServiceTest {

    @Mock
    private ChatModel chatModel;

    private VisitShortSummaryService service;

    @BeforeEach
    void setUp() {
        service = new VisitShortSummaryService(chatModel, new ObjectMapper(), new VisitProperties());
    }

    @Test
    void parsesShortSummaryJson() {
        VisitShortSummary summary = service.parse("""
                {
                  "visitName": "관절염 정기 진료",
                  "diagnosisFindings": ["파행", "관절 통증", "고령견"],
                  "oneLineSummary": "퇴행성 관절염 진단 (11세 고령견 해당)",
                  "careItems": ["산책 조절", "체중 관리", "진통제 복용"],
                  "careNote": "일주일 뒤 재방문",
                  "hospitalName": "행복동물병원"
                }
                """);

        assertThat(summary.visitName()).isEqualTo("관절염 정기 진료");
        assertThat(summary.diagnosisFindings()).hasSize(3);
        assertThat(summary.hospitalName()).isEqualTo("행복동물병원");
    }

    @Test
    void rejectsSummaryWithoutEnoughFindings() {
        assertThatThrownBy(() -> service.parse("""
                {
                  "visitName": "진료",
                  "diagnosisFindings": ["하나", "둘"],
                  "oneLineSummary": "요약",
                  "careItems": ["관리"]
                }
                """))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }
}

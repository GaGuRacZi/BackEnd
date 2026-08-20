package com.gaguraczi.paw.domain.visit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.enums.TranscriptSpeaker;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitShortSummaryServiceTest {

    @Mock
    private ChatModel chatModel;

    private VisitShortSummaryService service;

    @BeforeEach
    void setUp() {
        service = new VisitShortSummaryService(
                chatModel, new ObjectMapper(), new VisitProperties(),
                java.time.Clock.systemUTC()
        );
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
    void acceptsFewerFindingsAndCareItems() {
        VisitShortSummary summary = service.parse("""
                {
                  "visitName": "스케일링",
                  "diagnosisFindings": ["치석이 많아요", "잇몸 염증이 있어요"],
                  "oneLineSummary": "치석 제거와 잇몸 관리 안내를 받았어요.",
                  "careItems": ["일주일 동안 딱딱한 간식은 피해주세요"]
                }
                """);

        assertThat(summary.visitName()).isEqualTo("스케일링");
        assertThat(summary.diagnosisFindings()).containsExactly("치석이 많아요", "잇몸 염증이 있어요");
        assertThat(summary.careItems()).containsExactly("일주일 동안 딱딱한 간식은 피해주세요");
    }

    @Test
    void parsesTextualListFields() {
        VisitShortSummary summary = service.parse("""
                {
                  "visitName": "진료",
                  "diagnosisFindings": "파행\\n관절 통증",
                  "oneLineSummary": "요약",
                  "careItems": "- 산책 조절\\n- 체중 관리"
                }
                """);

        assertThat(summary.diagnosisFindings()).containsExactly("파행", "관절 통증");
        assertThat(summary.careItems()).containsExactly("산책 조절", "체중 관리");
    }

    @Test
    void rejectsSummaryWithoutVisitName() {
        assertThatThrownBy(() -> service.parse("""
                {
                  "diagnosisFindings": ["하나"],
                  "oneLineSummary": "요약",
                  "careItems": ["관리"]
                }
                """))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }

    @Test
    void rejectsSummaryWithoutOneLineSummary() {
        assertThatThrownBy(() -> service.parse("""
                {
                  "visitName": "진료",
                  "diagnosisFindings": ["하나"],
                  "careItems": ["관리"]
                }
                """))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }

    @Test
    void summarizeRetriesWhenFirstJsonIsInvalid() {
        Pet pet = Pet.builder().petName("초코").build();
        List<VisitSpeakerMapper.MappedTurn> turns = List.of(
                new VisitSpeakerMapper.MappedTurn(TranscriptSpeaker.VET, "잇몸 염증이 있어요.", 0.0, 1.0, 0)
        );
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(response("{"))
                .thenReturn(response("""
                        {
                          "visitName": "치과",
                          "diagnosisFindings": ["잇몸 염증"],
                          "oneLineSummary": "잇몸 염증 안내",
                          "careItems": ["양치"]
                        }
                        """));

        VisitShortSummary summary = service.summarize(turns, pet);

        assertThat(summary.visitName()).isEqualTo("치과");
        assertThat(summary.diagnosisFindings()).containsExactly("잇몸 염증");
        verify(chatModel, times(2)).call(any(Prompt.class));
    }

    private static ChatResponse response(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}

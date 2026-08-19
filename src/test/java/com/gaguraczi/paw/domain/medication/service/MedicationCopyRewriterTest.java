package com.gaguraczi.paw.domain.medication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.medication.config.MedicationProperties;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.model.MedicationCopy;
import com.gaguraczi.paw.domain.medication.model.MedicineStagingRow;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationCopyRewriterTest {

    @Mock
    private ChatModel chatModel;

    private MedicationCopyRewriter rewriter;

    @BeforeEach
    void setUp() {
        rewriter = new MedicationCopyRewriter(chatModel, new ObjectMapper(), new MedicationProperties());
    }

    @Test
    void parsesJsonObject() {
        MedicationCopy copy = rewriter.parse("""
                {"itemSeq":"1","descriptionMd":"관절 통증을 줄여주는 **NSAID**예요.","precautionMd":"- 식후에 주세요"}
                """);

        assertThat(copy.descriptionMd()).isEqualTo("관절 통증을 줄여주는 **NSAID**예요.");
        assertThat(copy.precautionMd()).isEqualTo("- 식후에 주세요");
    }

    @Test
    void parsesFencedJson() {
        MedicationCopy copy = rewriter.parse("""
                ```json
                {"descriptionMd":"샴푸예요.","precautionMd":"- 눈에 넣지 마세요"}
                ```
                """);

        assertThat(copy.descriptionMd()).isEqualTo("샴푸예요.");
        assertThat(copy.precautionMd()).isEqualTo("- 눈에 넣지 마세요");
    }

    @Test
    void parsesBatchArrayAndNewlines() {
        Map<String, MedicationCopy> copies = rewriter.parseBatch("""
                [
                  {
                    "itemSeq": "a",
                    "descriptionMd": "첫째 문단이에요.\\n\\n둘째 문단이에요.",
                    "precautionMd": "- 하나\\n- 둘"
                  },
                  {
                    "itemSeq": "b",
                    "descriptionMd": "샴푸예요.",
                    "precautionMd": "- 눈에 넣지 마세요"
                  }
                ]
                """);

        assertThat(copies.get("a").descriptionMd()).isEqualTo("첫째 문단이에요.\n\n둘째 문단이에요.");
        assertThat(copies.get("a").precautionMd()).isEqualTo("- 하나\n- 둘");
        assertThat(copies.get("b").descriptionMd()).isEqualTo("샴푸예요.");
    }

    @Test
    void rewriteBatchSendsOneChatCall() {
        when(chatModel.call(any(Prompt.class))).thenReturn(response("""
                [{"itemSeq":"2023","descriptionMd":"설명이에요.\\n\\n둘째 줄.","precautionMd":"- 주의\\n- 식후"}]
                """));
        MedicineStagingRow row = new MedicineStagingRow(
                "2023", "카미녹스", null, null, "통증", null, "주의", "개");

        Map<String, MedicationCopy> copies = rewriter.rewriteBatch(List.of(row));

        assertThat(copies.get("2023").descriptionMd()).contains("\n\n");
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    void rejectsMissingFields() {
        assertThatThrownBy(() -> rewriter.parse("{\"descriptionMd\":\"설명만\"}"))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
    }

    @Test
    void rejectsBlankPayload() {
        assertThatThrownBy(() -> rewriter.parse("   "))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
    }

    private static ChatResponse response(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}

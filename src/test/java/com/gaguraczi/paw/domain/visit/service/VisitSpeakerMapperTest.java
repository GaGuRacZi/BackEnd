package com.gaguraczi.paw.domain.visit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.visit.client.DiarizedSegment;
import com.gaguraczi.paw.domain.visit.client.DiarizedTranscript;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.enums.TranscriptSpeaker;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.support.VisitTextLimits;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitSpeakerMapperTest {

    @Mock
    private ChatModel chatModel;

    private VisitSpeakerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new VisitSpeakerMapper(chatModel, new ObjectMapper(), new VisitProperties());
    }

    @Test
    void mapsAnonymousLabelsToVetAndOwner() {
        DiarizedTranscript transcript = new DiarizedTranscript(
                null,
                80,
                List.of(
                        new DiarizedSegment("A", "보호자님, 아리 관절을 촉진해 보니 통증이 있어요. 퇴행성 관절염으로 보입니다.", 0.0, 8.0),
                        new DiarizedSegment("B", "아리가 요즘 다리를 절어요. 계단도 잘 못 올라가서 걱정돼요.", 8.0, 16.0),
                        new DiarizedSegment("A", "소염진통제를 처방해 드릴게요. 일주일 뒤에 다시 와 보세요.", 16.0, 24.0)
                )
        );
        Map<String, TranscriptSpeaker> roles = mapper.parseRoles("""
                {"A":"VET","B":"OWNER"}
                """);

        List<VisitSpeakerMapper.MappedTurn> turns = mapper.apply(transcript.segments(), roles);

        assertThat(turns).extracting(VisitSpeakerMapper.MappedTurn::speaker)
                .containsExactly(TranscriptSpeaker.VET, TranscriptSpeaker.OWNER, TranscriptSpeaker.VET);
        assertThat(turns).noneMatch(turn -> turn.speaker() == null);
        assertThat(turns.get(0).text()).contains("퇴행성 관절염");
        assertThat(turns.get(1).text()).contains("다리를 절어요");
    }

    @Test
    void acceptsKoreanRoleLabels() {
        Map<String, TranscriptSpeaker> roles = mapper.parseRoles("""
                {"A":"의사","B":"보호자"}
                """);

        assertThat(roles.get("A")).isEqualTo(TranscriptSpeaker.VET);
        assertThat(roles.get("B")).isEqualTo(TranscriptSpeaker.OWNER);
    }

    @Test
    void unmappedSpeakerFallsBackToOwner() {
        List<DiarizedSegment> segments = List.of(
                new DiarizedSegment("A", "문진을 시작할게요.", 0.0, 1.0),
                new DiarizedSegment("C", "안녕하세요", 1.0, 2.0)
        );

        List<VisitSpeakerMapper.MappedTurn> turns = mapper.apply(segments, Map.of("A", TranscriptSpeaker.VET));

        assertThat(turns).extracting(VisitSpeakerMapper.MappedTurn::speaker)
                .containsExactly(TranscriptSpeaker.VET, TranscriptSpeaker.OWNER);
    }

    @Test
    void failsWhenNoValidTurnsRemain() {
        List<DiarizedSegment> segments = List.of(
                new DiarizedSegment("C", "   ", 0.0, 1.0)
        );

        assertThatThrownBy(() -> mapper.apply(segments, Map.of("A", TranscriptSpeaker.VET)))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }

    @Test
    void payloadIncludesPetNameAndSpeakerBlocks() {
        DiarizedTranscript transcript = new DiarizedTranscript(
                null,
                10,
                List.of(
                        new DiarizedSegment("A", "문진을 시작할게요.", 0.0, 2.0),
                        new DiarizedSegment("B", "아리가 아파요.", 2.0, 4.0)
                )
        );

        String payload = VisitSpeakerMapper.buildPayload(transcript, "아리");

        assertThat(payload).contains("반려동물 이름: 아리");
        assertThat(payload).contains("### speaker A");
        assertThat(payload).contains("### speaker B");
        assertThat(payload).contains("아리가 아파요.");
    }

    @Test
    void payloadTruncatesEachSpeakerToVisitTextLimits() {
        String longText = "가".repeat(VisitTextLimits.SPEAKER_PROMPT_MAX_CHARS + 50);
        DiarizedTranscript transcript = new DiarizedTranscript(
                null,
                10,
                List.of(new DiarizedSegment("A", longText, 0.0, 2.0))
        );

        String payload = VisitSpeakerMapper.buildPayload(transcript, "아리");
        int header = payload.indexOf("### speaker A\n") + "### speaker A\n".length();
        String speakerBody = payload.substring(header).strip();

        assertThat(speakerBody).hasSize(VisitTextLimits.SPEAKER_PROMPT_MAX_CHARS);
    }

    @Test
    void inferRolesParsesJsonSurroundedByText() {
        DiarizedTranscript transcript = new DiarizedTranscript(
                null,
                10,
                List.of(
                        new DiarizedSegment("A", "문진을 시작할게요.", 0.0, 2.0),
                        new DiarizedSegment("B", "아리가 아파요.", 2.0, 4.0)
                )
        );
        when(chatModel.call(any(Prompt.class))).thenReturn(response("""
                [주의] 화자 매핑 결과입니다.
                {"A":"VET","B":"OWNER"}
                """));

        Map<String, TranscriptSpeaker> roles = mapper.inferRoles(transcript, "아리");

        assertThat(roles.get("A")).isEqualTo(TranscriptSpeaker.VET);
        assertThat(roles.get("B")).isEqualTo(TranscriptSpeaker.OWNER);
    }

    @Test
    void inferRolesConvertsRuntimeExceptionToSummaryFailed() {
        DiarizedTranscript transcript = new DiarizedTranscript(
                null,
                10,
                List.of(new DiarizedSegment("A", "문진을 시작할게요.", 0.0, 2.0))
        );
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> mapper.inferRoles(transcript, "아리"))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }

    @Test
    void parseRolesFailsForNonObjectJson() {
        assertThatThrownBy(() -> mapper.parseRoles("[]"))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }

    private static ChatResponse response(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}

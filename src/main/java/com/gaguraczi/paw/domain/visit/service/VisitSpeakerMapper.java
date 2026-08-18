package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.visit.client.DiarizedSegment;
import com.gaguraczi.paw.domain.visit.client.DiarizedTranscript;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.enums.TranscriptSpeaker;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.support.VisitJsonText;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitSpeakerMapper {

    static final String SYSTEM_PROMPT = """
            너는 동물병원 진료 대화의 화자를 분류한다.
            각 speaker 라벨(A, B 등)을 VET(의사) 또는 OWNER(보호자) 중 하나로만 매핑한다.
            VET: 문진, 촉진, 영상 소견, 진단, 처방, "보호자님" 호칭.
            OWNER: 증상 호소, 걱정, 질문, 반려동물 이름 호칭.
            JSON만 출력한다. 예: {"A":"VET","B":"OWNER"}
            설명 문장을 붙이지 마라.
            """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final VisitProperties visitProperties;

    public List<MappedTurn> map(DiarizedTranscript transcript, String petName) {
        if (transcript == null || transcript.segments() == null || transcript.segments().isEmpty()) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
        }
        Map<String, TranscriptSpeaker> roles = inferRoles(transcript, petName);
        return apply(transcript.segments(), roles);
    }

    List<MappedTurn> apply(List<DiarizedSegment> segments, Map<String, TranscriptSpeaker> roles) {
        List<MappedTurn> turns = new ArrayList<>();
        int order = 0;
        for (DiarizedSegment segment : segments) {
            if (segment.text() == null || segment.text().isBlank()) {
                continue;
            }
            TranscriptSpeaker speaker = roles.get(normalizeSpeaker(segment.speaker()));
            if (speaker == null) {
                throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
            }
            turns.add(new MappedTurn(speaker, segment.text().trim(), segment.startSec(), segment.endSec(), order++));
        }
        if (turns.isEmpty()) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
        }
        return List.copyOf(turns);
    }

    Map<String, TranscriptSpeaker> inferRoles(DiarizedTranscript transcript, String petName) {
        String payload = buildPayload(transcript, petName);
        try {
            var options = OpenAiChatOptions.builder()
                    .model(visitProperties.getChatModel())
                    .reasoningEffort(visitProperties.getReasoningEffort())
                    .maxCompletionTokens(400)
                    .build();
            Prompt prompt = new Prompt(
                    List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(payload)),
                    options
            );
            String text = chatModel.call(prompt).getResult().getOutput().getText();
            return parseRoles(text);
        } catch (GeneralException e) {
            throw e;
        } catch (RuntimeException e) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED, e);
        }
    }

    Map<String, TranscriptSpeaker> parseRoles(String raw) {
        try {
            JsonNode node = objectMapper.readTree(VisitJsonText.extractJson(raw));
            if (!node.isObject()) {
                throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
            }
            Map<String, TranscriptSpeaker> roles = new HashMap<>();
            var fields = node.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                TranscriptSpeaker speaker = parseSpeaker(entry.getValue().asText(""));
                if (speaker == null) {
                    continue;
                }
                roles.put(normalizeSpeaker(entry.getKey()), speaker);
            }
            if (roles.isEmpty()) {
                throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
            }
            return roles;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED, e);
        }
    }

    static String buildPayload(DiarizedTranscript transcript, String petName) {
        StringBuilder sb = new StringBuilder();
        if (petName != null && !petName.isBlank()) {
            sb.append("반려동물 이름: ").append(petName.trim()).append('\n');
        }
        sb.append("화자별 발화:\n");
        Map<String, StringBuilder> bySpeaker = new LinkedHashMap<>();
        for (DiarizedSegment segment : transcript.segments()) {
            String speaker = normalizeSpeaker(segment.speaker());
            bySpeaker.computeIfAbsent(speaker, key -> new StringBuilder())
                    .append(segment.text() == null ? "" : segment.text().trim())
                    .append('\n');
        }
        for (Map.Entry<String, StringBuilder> entry : bySpeaker.entrySet()) {
            sb.append("### speaker ").append(entry.getKey()).append('\n');
            sb.append(entry.getValue()).append('\n');
        }
        return sb.toString();
    }

    static String normalizeSpeaker(String speaker) {
        if (speaker == null || speaker.isBlank()) {
            return "A";
        }
        return speaker.trim().toUpperCase(Locale.ROOT);
    }

    private static TranscriptSpeaker parseSpeaker(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("VET".equals(normalized) || "DOCTOR".equals(normalized) || "의사".equals(value.trim())) {
            return TranscriptSpeaker.VET;
        }
        if ("OWNER".equals(normalized) || "GUARDIAN".equals(normalized) || "보호자".equals(value.trim())) {
            return TranscriptSpeaker.OWNER;
        }
        return null;
    }

    public record MappedTurn(
            TranscriptSpeaker speaker,
            String text,
            Double startSec,
            Double endSec,
            int sortOrder
    ) {
    }
}

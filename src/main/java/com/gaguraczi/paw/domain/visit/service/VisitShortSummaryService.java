package com.gaguraczi.paw.domain.visit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.enums.TranscriptSpeaker;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.support.VisitJsonText;
import com.gaguraczi.paw.domain.visit.support.VisitPetDisplay;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitShortSummaryService {

    private static final int MAX_FINDINGS = 4;
    private static final int MAX_CARE_ITEMS = 5;
    private static final int RAW_LOG_LIMIT = 2000;

    static final String SYSTEM_PROMPT = """
            너는 반려동물 진료 대화를 보호자가 읽기 쉽게 정리한다.
            전사문에 없는 진단·처방·병원명을 지어내지 마라.
            반드시 JSON만 출력한다. 키:
            visitName (진료명, 짧은 제목, 필수),
            diagnosisFindings (증상/소견 불릿 문자열 배열, 1~4개. 없으면 있는 것만),
            oneLineSummary (한줄 진단 요약, 필수),
            careItems (치료 및 관리 불릿 문자열 배열, 1~5개. 없으면 있는 것만),
            careNote (재방문 등 하단 한 줄, 없으면 빈 문자열),
            hospitalName (병원명, 없으면 null).
            """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final VisitProperties visitProperties;

    public VisitShortSummary summarize(List<VisitSpeakerMapper.MappedTurn> turns, Pet pet) {
        String payload = buildPayload(turns, pet);
        try {
            String text = callModel(payload);
            try {
                return parse(text);
            } catch (GeneralException first) {
                log.warn("Visit short summary parse failed, retrying. raw={}", truncateRaw(text));
                String retryPayload = payload + """
                        
                        
                        이전 JSON이 형식에 맞지 않았다. visitName과 oneLineSummary는 비우지 마라.
                        diagnosisFindings와 careItems는 전사에 있는 내용만 문자열 배열로 넣어라.
                        없는 내용을 채우지 마라. JSON만 출력하라.
                        초안:
                        """ + nvl(text);
                String retried = callModel(retryPayload);
                try {
                    return parse(retried);
                } catch (GeneralException retryFailed) {
                    log.warn("Visit short summary parse retry failed. raw={}", truncateRaw(retried));
                    throw retryFailed;
                }
            }
        } catch (GeneralException e) {
            throw e;
        } catch (RuntimeException e) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED, e);
        }
    }

    private String callModel(String userPayload) {
        var options = OpenAiChatOptions.builder()
                .model(visitProperties.getChatModel())
                .reasoningEffort(visitProperties.getReasoningEffort())
                .maxCompletionTokens(1200)
                .build();
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(userPayload)),
                options
        );
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    VisitShortSummary parse(String raw) {
        try {
            JsonNode node = objectMapper.readTree(VisitJsonText.extractJson(raw));
            String visitName = text(node, "visitName");
            String oneLine = text(node, "oneLineSummary");
            List<String> findings = cap(stringList(node, "diagnosisFindings"), MAX_FINDINGS);
            List<String> careItems = cap(stringList(node, "careItems"), MAX_CARE_ITEMS);
            if (visitName == null || oneLine == null) {
                throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
            }
            String hospital = text(node, "hospitalName");
            return new VisitShortSummary(
                    visitName,
                    List.copyOf(findings),
                    oneLine,
                    List.copyOf(careItems),
                    nvl(text(node, "careNote")),
                    hospital
            );
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED, e);
        }
    }

    static String buildPayload(List<VisitSpeakerMapper.MappedTurn> turns, Pet pet) {
        StringBuilder sb = new StringBuilder();
        sb.append("반려동물: ").append(pet.getPetName());
        String breed = VisitPetDisplay.breedName(pet);
        if (breed != null && !breed.isBlank()) {
            sb.append(" / 품종: ").append(breed);
        }
        String age = VisitPetDisplay.ageLabel(pet.getBirth());
        if (age != null) {
            sb.append(" / 나이: ").append(age);
        }
        sb.append("\n\n전사문:\n");
        for (VisitSpeakerMapper.MappedTurn turn : turns) {
            String label = turn.speaker() == TranscriptSpeaker.VET ? "의사" : "보호자";
            sb.append(label).append(": ").append(turn.text()).append('\n');
        }
        return sb.toString();
    }

    private static List<String> stringList(JsonNode node, String field) {
        JsonNode value = node.get(field);
        List<String> values = new ArrayList<>();
        if (value == null || value.isNull()) {
            return values;
        }
        if (value.isTextual()) {
            addSplitItems(values, value.asText(""));
            return values;
        }
        if (!value.isArray()) {
            return values;
        }
        for (JsonNode item : value) {
            if (item != null && item.isTextual()) {
                addSplitItems(values, item.asText(""));
            } else if (item != null) {
                String text = item.asText("").trim();
                if (!text.isEmpty()) {
                    values.add(text);
                }
            }
        }
        return values;
    }

    private static void addSplitItems(List<String> values, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        String[] parts = raw.split("[\\n;]+");
        for (String part : parts) {
            String text = part.replaceFirst("^[-*•]\\s*", "").trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
    }

    private static List<String> cap(List<String> values, int max) {
        if (values.size() <= max) {
            return values;
        }
        return values.subList(0, max);
    }

    private static String truncateRaw(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        if (trimmed.length() <= RAW_LOG_LIMIT) {
            return trimmed;
        }
        return trimmed.substring(0, RAW_LOG_LIMIT) + "...";
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText("").trim();
        return text.isEmpty() ? null : text;
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}

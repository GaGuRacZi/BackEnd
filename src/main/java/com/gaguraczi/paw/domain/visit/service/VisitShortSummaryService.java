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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VisitShortSummaryService {

    static final String SYSTEM_PROMPT = """
            너는 반려동물 진료 대화를 보호자가 읽기 쉽게 정리한다.
            전사문에 없는 진단·처방·병원명을 지어내지 마라.
            반드시 JSON만 출력한다. 키:
            visitName (진료명, 짧은 제목),
            diagnosisFindings (증상/소견 불릿 3~4개 문자열 배열),
            oneLineSummary (한줄 진단 요약),
            careItems (치료 및 관리 불릿 3~5개),
            careNote (재방문 등 하단 한 줄, 없으면 빈 문자열),
            hospitalName (병원명, 없으면 null).
            """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final VisitProperties visitProperties;

    public VisitShortSummary summarize(List<VisitSpeakerMapper.MappedTurn> turns, Pet pet) {
        try {
            var options = OpenAiChatOptions.builder()
                    .model(visitProperties.getChatModel())
                    .reasoningEffort(visitProperties.getReasoningEffort())
                    .maxCompletionTokens(1200)
                    .build();
            Prompt prompt = new Prompt(
                    List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(buildPayload(turns, pet))),
                    options
            );
            String text = chatModel.call(prompt).getResult().getOutput().getText();
            return parse(text);
        } catch (GeneralException e) {
            throw e;
        } catch (RuntimeException e) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED, e);
        }
    }

    VisitShortSummary parse(String raw) {
        try {
            JsonNode node = objectMapper.readTree(VisitJsonText.extractJson(raw));
            String visitName = text(node, "visitName");
            String oneLine = text(node, "oneLineSummary");
            List<String> findings = stringList(node, "diagnosisFindings");
            List<String> careItems = stringList(node, "careItems");
            if (visitName == null || oneLine == null || findings.size() < 3 || careItems.size() < 3) {
                throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
            }
            if (findings.size() > 4) {
                findings = findings.subList(0, 4);
            }
            if (careItems.size() > 5) {
                careItems = careItems.subList(0, 5);
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
        JsonNode array = node.get(field);
        List<String> values = new ArrayList<>();
        if (array == null || !array.isArray()) {
            return values;
        }
        for (JsonNode item : array) {
            String text = item.asText("").trim();
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
        return values;
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

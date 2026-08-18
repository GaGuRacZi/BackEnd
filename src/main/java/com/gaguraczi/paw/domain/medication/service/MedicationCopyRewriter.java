package com.gaguraczi.paw.domain.medication.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.medication.config.MedicationProperties;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.model.MedicationCopy;
import com.gaguraczi.paw.domain.medication.model.MedicineStagingRow;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("medication-ingest")
@RequiredArgsConstructor
public class MedicationCopyRewriter {

    static final String SYSTEM_PROMPT = """
            너는 동물병원 처방 약을 보호자가 읽기 쉽게 다듬는 역할이다.
            원문에 없는 효능·성분·주의사항을 지어내지 마라.
            해요체를 쓰고, 전문 용어가 나오면 괄호나 짧은 풀어쓰기를 곁들여라.
            제목 행(약 설명, 주의할 점)은 본문에 넣지 마라.

            반드시 JSON만 출력한다. 여러 건이면 배열, 한 건이면 객체.
            각 원소 키: itemSeq, descriptionMd, precautionMd.

            descriptionMd (약 설명):
            - 2~4문장, 약 종류는 **볼드**.
            - 문단을 나눠라. 한 문단은 1~2문장.
            - 문단 사이에는 빈 줄이 있어야 한다.
            - 한 줄로 이어 붙이지 마라.

            precautionMd (주의할 점):
            - 마크다운 불릿 2~5개.
            - 각 불릿은 새 줄에서 `- `로 시작한다.
            - 불릿을 한 줄에 이어 쓰지 마라.

            JSON 문자열 안의 줄바꿈은 반드시 \\n 으로 이스케이프한다.
            예:
            {"itemSeq":"1","descriptionMd":"관절 염증과 통증을 줄여주는 **NSAID**예요.\\n\\n프로스타글란딘 합성을 억제해 염증을 가라앉혀요.","precautionMd":"- 다른 소염제와 함께 쓰면 안 돼요\\n- 식후에 주세요"}
            """;

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final MedicationProperties medicationProperties;

    public Map<String, MedicationCopy> rewriteBatch(List<MedicineStagingRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        try {
            return callModel(rows);
        } catch (RuntimeException batchError) {
            if (rows.size() == 1) {
                throw batchError instanceof GeneralException
                        ? (GeneralException) batchError
                        : GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED, batchError);
            }
            log.warn("medication batch rewrite failed, falling back per item: {}", batchError.toString());
            Map<String, MedicationCopy> copies = new LinkedHashMap<>();
            for (MedicineStagingRow row : rows) {
                try {
                    copies.putAll(callModel(List.of(row)));
                } catch (RuntimeException e) {
                    log.warn("medication rewrite skipped item_seq={} ({})", row.itemSeq(), e.toString());
                }
            }
            return copies;
        }
    }

    private Map<String, MedicationCopy> callModel(List<MedicineStagingRow> rows) {
        try {
            int maxTokens = Math.min(8000, 500 + rows.size() * 450);
            var options = OpenAiChatOptions.builder()
                    .model(medicationProperties.getChatModel())
                    .reasoningEffort(medicationProperties.getReasoningEffort())
                    .maxCompletionTokens(maxTokens)
                    .build();
            Prompt prompt = new Prompt(
                    List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(batchPayload(rows))),
                    options
            );
            String text = chatModel.call(prompt).getResult().getOutput().getText();
            Map<String, MedicationCopy> parsed = parseBatch(text);
            Map<String, MedicationCopy> matched = new LinkedHashMap<>();
            for (MedicineStagingRow row : rows) {
                MedicationCopy copy = parsed.get(row.itemSeq());
                if (copy != null) {
                    matched.put(row.itemSeq(), copy);
                }
            }
            if (matched.isEmpty()) {
                throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
            }
            return matched;
        } catch (GeneralException e) {
            throw e;
        } catch (RuntimeException e) {
            throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED, e);
        }
    }

    Map<String, MedicationCopy> parseBatch(String raw) {
        try {
            JsonNode node = objectMapper.readTree(extractJson(raw));
            Map<String, MedicationCopy> copies = new LinkedHashMap<>();
            if (node.isArray()) {
                for (JsonNode item : node) {
                    putCopy(copies, item);
                }
            } else if (node.isObject()) {
                putCopy(copies, node);
            } else {
                throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
            }
            if (copies.isEmpty()) {
                throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
            }
            return copies;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED, e);
        }
    }

    MedicationCopy parse(String raw) {
        Map<String, MedicationCopy> copies = parseBatch(raw);
        return copies.values().iterator().next();
    }

    private void putCopy(Map<String, MedicationCopy> copies, JsonNode node) {
        String description = normalizeBreaks(text(node, "descriptionMd"));
        String precaution = normalizeBreaks(text(node, "precautionMd"));
        if (description == null || precaution == null) {
            return;
        }
        String itemSeq = text(node, "itemSeq");
        if (itemSeq == null) {
            itemSeq = "_" + copies.size();
        }
        copies.put(itemSeq, new MedicationCopy(description, precaution));
    }

    private static String batchPayload(List<MedicineStagingRow> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("아래 약물 ").append(rows.size()).append("건을 각각 다듬어 JSON 배열로 반환해라.\n");
        sb.append("배열 길이는 입력과 같고, 각 원소의 itemSeq는 입력 값을 그대로 써라.\n\n");
        int index = 1;
        for (MedicineStagingRow row : rows) {
            sb.append("### ").append(index++).append(". itemSeq=").append(row.itemSeq()).append('\n');
            sb.append(userPayload(row)).append('\n');
        }
        return sb.toString();
    }

    private static String userPayload(MedicineStagingRow row) {
        return """
                제품명: %s
                영문명: %s
                성분: %s
                대상동물: %s
                효능: %s
                용법: %s
                주의사항: %s
                """.formatted(
                nvl(row.productName()),
                nvl(row.productNameEn()),
                nvl(row.ingredients()),
                nvl(row.targetAnimal()),
                nvl(row.efficacy()),
                nvl(row.dosage()),
                nvl(row.precaution())
        );
    }

    static String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
        }
        String trimmed = raw.trim();
        int arrayStart = trimmed.indexOf('[');
        int objectStart = trimmed.indexOf('{');
        if (arrayStart >= 0 && (objectStart < 0 || arrayStart < objectStart)) {
            int end = trimmed.lastIndexOf(']');
            if (end <= arrayStart) {
                throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
            }
            return trimmed.substring(arrayStart, end + 1);
        }
        int end = trimmed.lastIndexOf('}');
        if (objectStart < 0 || end <= objectStart) {
            throw GeneralException.of(MedicationErrorCode.MEDICATION_REWRITE_FAILED);
        }
        return trimmed.substring(objectStart, end + 1);
    }

    static String normalizeBreaks(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r\n", "\n").replace("\\n", "\n");
        normalized = normalized.replaceAll(" *\\n *", "\n").trim();
        return normalized.isEmpty() ? null : normalized;
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
        return value == null || value.isBlank() ? "(없음)" : value.trim();
    }
}

package com.gaguraczi.paw.domain.rag.support;

import tools.jackson.databind.JsonNode;
import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.dto.RagSearchHit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class OpenAiFileSearchResponseParser {

    private OpenAiFileSearchResponseParser() {
    }

    public static RagAskResult parse(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return new RagAskResult("", List.of());
        }
        StringBuilder answer = new StringBuilder();
        List<RagSearchHit> sources = new ArrayList<>();
        List<RagSearchHit> citations = new ArrayList<>();
        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                String type = item.path("type").asString("");
                if ("file_search_call".equals(type)) {
                    appendSources(sources, firstArray(item, "results", "search_results"));
                }
                if ("message".equals(type)) {
                    appendMessageText(answer, item.path("content"));
                    appendCitations(citations, item.path("content"));
                }
            }
        }
        if (answer.isEmpty()) {
            String outputText = root.path("output_text").asString("");
            if (!outputText.isBlank()) {
                answer.append(outputText);
            }
        }
        if (sources.isEmpty()) {
            sources.addAll(dedupe(citations));
        }
        return new RagAskResult(answer.toString().trim(), List.copyOf(sources));
    }

    private static void appendMessageText(StringBuilder answer, JsonNode content) {
        if (!content.isArray()) {
            return;
        }
        for (JsonNode part : content) {
            String type = part.path("type").asString("");
            if ("output_text".equals(type) || "text".equals(type)) {
                String text = part.path("text").asString("");
                if (!text.isBlank()) {
                    if (!answer.isEmpty()) {
                        answer.append('\n');
                    }
                    answer.append(text);
                }
            }
        }
    }

    private static void appendCitations(List<RagSearchHit> citations, JsonNode content) {
        if (!content.isArray()) {
            return;
        }
        for (JsonNode part : content) {
            JsonNode annotations = part.path("annotations");
            if (!annotations.isArray()) {
                continue;
            }
            for (JsonNode annotation : annotations) {
                String type = annotation.path("type").asString("");
                if (!"file_citation".equals(type) && !"container_file_citation".equals(type)) {
                    continue;
                }
                citations.add(OpenAiVectorStoreHitMapper.map(
                        text(annotation, "file_id"),
                        text(annotation, "filename"),
                        "",
                        0d
                ));
            }
        }
    }

    private static void appendSources(List<RagSearchHit> sources, JsonNode results) {
        if (results == null || !results.isArray()) {
            return;
        }
        for (JsonNode result : results) {
            sources.add(OpenAiVectorStoreHitMapper.map(
                    text(result, "file_id"),
                    text(result, "filename"),
                    resultText(result),
                    result.path("score").asDouble(0d)
            ));
        }
    }

    private static JsonNode firstArray(JsonNode item, String... fields) {
        for (String field : fields) {
            JsonNode node = item.path(field);
            if (node.isArray()) {
                return node;
            }
        }
        return item.path("results");
    }

    private static List<RagSearchHit> dedupe(List<RagSearchHit> hits) {
        Map<String, RagSearchHit> unique = new LinkedHashMap<>();
        for (RagSearchHit hit : hits) {
            String key = String.valueOf(hit.fileId()) + "|" + String.valueOf(hit.fileName());
            unique.putIfAbsent(key, hit);
        }
        return List.copyOf(unique.values());
    }

    private static String resultText(JsonNode result) {
        String direct = result.path("text").asString("");
        if (!direct.isBlank()) {
            return direct;
        }
        JsonNode content = result.path("content");
        if (!content.isArray()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (JsonNode part : content) {
            String text = part.path("text").asString("");
            if (!text.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append('\n');
                }
                builder.append(text);
            }
        }
        return builder.toString();
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asString("");
        return value.isBlank() ? null : value;
    }
}

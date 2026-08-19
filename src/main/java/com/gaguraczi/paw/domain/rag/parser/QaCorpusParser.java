package com.gaguraczi.paw.domain.rag.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import com.gaguraczi.paw.domain.rag.model.RagChunk;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class QaCorpusParser {

    private final ObjectMapper objectMapper;

    public QaCorpusParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<RagChunk> parse(Path file) throws IOException {
        JsonNode root = objectMapper.readTree(file.toFile());
        JsonNode qa = root.path("qa");
        String input = text(qa.path("input"));
        String output = text(qa.path("output"));
        if (input.isBlank() || output.isBlank()) {
            return Optional.empty();
        }

        JsonNode meta = root.path("meta");
        String department = blankToNull(text(meta.path("department")));
        String lifeCycle = blankToNull(text(meta.path("lifeCycle")));
        String disease = blankToNull(text(meta.path("disease")));

        StringBuilder header = new StringBuilder();
        appendHeader(header, "과목", department);
        appendHeader(header, "생애주기", lifeCycle);
        if (disease != null && !"기타".equals(disease)) {
            appendHeader(header, "질환", disease);
        }

        if (!header.isEmpty()) {
            header.append('\n');
        }

        String content = header
                + "[질문]\n" + input
                + "\n[답변]\n" + output;

        return Optional.of(new RagChunk(
                sourceId(file),
                0,
                RagSourceType.QA,
                department,
                lifeCycle,
                disease,
                null,
                content
        ));
    }

    public List<RagChunk> parseToList(Path file) throws IOException {
        return parse(file).map(List::of).orElseGet(List::of);
    }

    static String sourceId(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    static String text(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? "" : node.asText("").trim();
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    static void appendHeader(StringBuilder header, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!header.isEmpty()) {
            header.append(" | ");
        }
        header.append(label).append(": ").append(value);
    }
}

package com.gaguraczi.paw.domain.rag.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.rag.chunking.CharacterTextSplitter;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import com.gaguraczi.paw.domain.rag.model.RagChunk;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SourceCorpusParser {

    private final ObjectMapper objectMapper;
    private final CharacterTextSplitter splitter;

    public SourceCorpusParser(ObjectMapper objectMapper, CharacterTextSplitter splitter) {
        this.objectMapper = objectMapper;
        this.splitter = splitter;
    }

    public List<RagChunk> parse(Path file) throws IOException {
        JsonNode root = objectMapper.readTree(file.toFile());
        String title = QaCorpusParser.blankToNull(QaCorpusParser.text(root.path("title")));
        String author = QaCorpusParser.blankToNull(QaCorpusParser.text(root.path("author")));
        String department = QaCorpusParser.blankToNull(QaCorpusParser.text(root.path("department")));
        String body = QaCorpusParser.text(root.path("disease"));
        if (body.isBlank()) {
            return List.of();
        }

        StringBuilder header = new StringBuilder();
        QaCorpusParser.appendHeader(header, "과목", department);
        QaCorpusParser.appendHeader(header, "제목", title);
        QaCorpusParser.appendHeader(header, "저자", author);
        String prefix = header.isEmpty() ? "" : header + "\n";

        List<String> parts = splitter.split(body);
        List<RagChunk> chunks = new ArrayList<>(parts.size());
        String sourceId = QaCorpusParser.sourceId(file);
        for (int i = 0; i < parts.size(); i++) {
            chunks.add(new RagChunk(
                    sourceId,
                    i,
                    RagSourceType.CORPUS,
                    department,
                    null,
                    null,
                    title,
                    prefix + parts.get(i)
            ));
        }
        return chunks;
    }
}

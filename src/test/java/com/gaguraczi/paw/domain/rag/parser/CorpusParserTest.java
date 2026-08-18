package com.gaguraczi.paw.domain.rag.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.rag.chunking.CharacterTextSplitter;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import com.gaguraczi.paw.domain.rag.model.RagChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CorpusParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final QaCorpusParser qaParser = new QaCorpusParser(objectMapper);
    private final SourceCorpusParser sourceParser = new SourceCorpusParser(
            objectMapper, new CharacterTextSplitter(80, 10));

    @TempDir
    Path tempDir;

    @Test
    void parsesQaAsSingleChunk() throws Exception {
        Path file = tempDir.resolve("qa-sample.json");
        Files.writeString(file, """
                {
                  "meta": {"lifeCycle": "노령견", "department": "내과", "disease": "관절염"},
                  "qa": {
                    "instruction": "전문가로 답변해줘",
                    "input": "앞다리를 절뚝거려요.",
                    "output": "퇴행성 관절염을 의심할 수 있습니다."
                  }
                }
                """);

        Optional<RagChunk> parsed = qaParser.parse(file);
        assertThat(parsed).isPresent();
        RagChunk chunk = parsed.get();
        assertThat(chunk.sourceId()).isEqualTo("qa-sample");
        assertThat(chunk.chunkIndex()).isZero();
        assertThat(chunk.sourceType()).isEqualTo(RagSourceType.QA);
        assertThat(chunk.department()).isEqualTo("내과");
        assertThat(chunk.lifeCycle()).isEqualTo("노령견");
        assertThat(chunk.disease()).isEqualTo("관절염");
        assertThat(chunk.content()).contains("과목: 내과 | 생애주기: 노령견 | 질환: 관절염");
        assertThat(chunk.content()).contains("[질문]").contains("앞다리를 절뚝거려요.");
        assertThat(chunk.content()).contains("[답변]").contains("퇴행성 관절염");
    }

    @Test
    void skips기타DiseaseInQaHeader() throws Exception {
        Path file = tempDir.resolve("qa-etc.json");
        Files.writeString(file, """
                {
                  "meta": {"lifeCycle": "성견", "department": "피부과", "disease": "기타"},
                  "qa": {"input": "가려워요", "output": "알레르기를 의심합니다."}
                }
                """);

        RagChunk chunk = qaParser.parse(file).orElseThrow();
        assertThat(chunk.content()).contains("과목: 피부과 | 생애주기: 성견");
        assertThat(chunk.content()).doesNotContain("질환: 기타");
        assertThat(chunk.disease()).isEqualTo("기타");
    }

    @Test
    void skipsQaWhenInputOrOutputBlank() throws Exception {
        Path missingOutput = tempDir.resolve("qa-missing-output.json");
        Files.writeString(missingOutput, """
                {
                  "qa": {"input": "앞다리를 절어요", "output": "  "}
                }
                """);
        Path missingInput = tempDir.resolve("qa-missing-input.json");
        Files.writeString(missingInput, """
                {
                  "qa": {"input": "", "output": "병원에서 진료받으세요."}
                }
                """);

        assertThat(qaParser.parse(missingOutput)).isEmpty();
        assertThat(qaParser.parse(missingInput)).isEmpty();
    }

    @Test
    void splitsCorpusBodyIntoChunks() throws Exception {
        Path file = tempDir.resolve("corpus-sample.json");
        Files.writeString(file, """
                {
                  "title": "소동물 신장학",
                  "author": "현창백",
                  "department": "내과",
                  "disease": "신부전 환자를 평가할 때는 질산혈증과 요독증을 구분해야 합니다. 신전성, 신성, 신후성 원인을 감별하세요. 만성 신부전은 완치보다 관리가 목표입니다. 식이와 혈압 조절이 중요합니다. 단백뇨가 있으면 UPC를 추적합니다."
                }
                """);

        List<RagChunk> chunks = sourceParser.parse(file);
        assertThat(chunks).isNotEmpty();
        assertThat(chunks.getFirst().sourceType()).isEqualTo(RagSourceType.CORPUS);
        assertThat(chunks.getFirst().department()).isEqualTo("내과");
        assertThat(chunks.getFirst().title()).isEqualTo("소동물 신장학");
        assertThat(chunks.getFirst().content()).contains("제목: 소동물 신장학");
        assertThat(chunks.getFirst().lifeCycle()).isNull();
        assertThat(chunks.getFirst().disease()).isNull();
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).chunkIndex()).isEqualTo(i);
            assertThat(chunks.get(i).sourceId()).isEqualTo("corpus-sample");
        }
    }
}

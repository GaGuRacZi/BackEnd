package com.gaguraczi.paw.domain.rag.support;

import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiFileSearchResponseParserTest {

    private final JsonMapper jsonMapper = JsonMapper.shared();

    @Test
    void parsesAnswerAndSources() {
        String json = """
                {
                  "output": [
                    {
                      "type": "file_search_call",
                      "results": [
                        {
                          "file_id": "file-1",
                          "filename": "내과_QA_000.md",
                          "score": 0.91,
                          "text": "source_id: SRC-1 | chunk: 0 | type: QA\\n과목: 내과 | 생애주기: 노령견 | 질환: 관절염\\n[질문]\\n앞다리를 절어요"
                        }
                      ]
                    },
                    {
                      "type": "message",
                      "content": [
                        { "type": "output_text", "text": "노령견 관절염일 수 있어요." }
                      ]
                    }
                  ]
                }
                """;

        RagAskResult result = OpenAiFileSearchResponseParser.parse(jsonMapper.readTree(json));

        assertThat(result.answer()).isEqualTo("노령견 관절염일 수 있어요.");
        assertThat(result.sources()).hasSize(1);
        assertThat(result.sources().getFirst().sourceId()).isEqualTo("SRC-1");
        assertThat(result.sources().getFirst().department()).isEqualTo("내과");
        assertThat(result.sources().getFirst().sourceType()).isEqualTo(RagSourceType.QA);
        assertThat(result.sources().getFirst().score()).isEqualTo(0.91);
    }

    @Test
    void fallsBackToOutputText() {
        String json = """
                {
                  "output_text": "자료에 관절 관리 안내가 있어요."
                }
                """;

        RagAskResult result = OpenAiFileSearchResponseParser.parse(jsonMapper.readTree(json));

        assertThat(result.answer()).isEqualTo("자료에 관절 관리 안내가 있어요.");
        assertThat(result.sources()).isEmpty();
    }

    @Test
    void handlesNullOrEmptyResponse() {
        RagAskResult nullResult = OpenAiFileSearchResponseParser.parse(null);
        assertThat(nullResult.answer()).isEmpty();
        assertThat(nullResult.sources()).isEmpty();

        RagAskResult jsonNullResult = OpenAiFileSearchResponseParser.parse(jsonMapper.readTree("null"));
        assertThat(jsonNullResult.answer()).isEmpty();
        assertThat(jsonNullResult.sources()).isEmpty();

        RagAskResult emptyResult = OpenAiFileSearchResponseParser.parse(jsonMapper.readTree("{}"));
        assertThat(emptyResult.answer()).isEmpty();
        assertThat(emptyResult.sources()).isEmpty();
    }
}

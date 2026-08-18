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
        assertThat(result.sources().getFirst().fileName()).isEqualTo("내과_QA_000.md");
    }

    @Test
    void parsesSearchResultsFieldName() {
        String json = """
                {
                  "output": [
                    {
                      "type": "file_search_call",
                      "queries": ["치아 흡수성 병변"],
                      "search_results": [
                        {
                          "file_id": "file-2",
                          "filename": "치과_QA_001.md",
                          "score": 0.88,
                          "text": "source_id: DENT-1 | chunk: 0 | type: QA\\n과목: 치과 | 질환: 치아흡수\\n스케일링 후 관리"
                        }
                      ]
                    },
                    {
                      "type": "message",
                      "content": [
                        { "type": "output_text", "text": "치과 처치 안내예요." }
                      ]
                    }
                  ]
                }
                """;

        RagAskResult result = OpenAiFileSearchResponseParser.parse(jsonMapper.readTree(json));

        assertThat(result.sources()).hasSize(1);
        assertThat(result.sources().getFirst().sourceId()).isEqualTo("DENT-1");
        assertThat(result.sources().getFirst().fileName()).isEqualTo("치과_QA_001.md");
    }

    @Test
    void fallsBackToFileCitationsWhenResultsMissing() {
        String json = """
                {
                  "output": [
                    {
                      "type": "file_search_call",
                      "queries": ["스케일링"],
                      "search_results": null
                    },
                    {
                      "type": "message",
                      "content": [
                        {
                          "type": "output_text",
                          "text": "스케일링 후 관리가 중요해요.",
                          "annotations": [
                            {
                              "type": "file_citation",
                              "file_id": "file-9",
                              "filename": "치과_CORPUS_000.md"
                            },
                            {
                              "type": "file_citation",
                              "file_id": "file-9",
                              "filename": "치과_CORPUS_000.md"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """;

        RagAskResult result = OpenAiFileSearchResponseParser.parse(jsonMapper.readTree(json));

        assertThat(result.sources()).hasSize(1);
        assertThat(result.sources().getFirst().fileName()).isEqualTo("치과_CORPUS_000.md");
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

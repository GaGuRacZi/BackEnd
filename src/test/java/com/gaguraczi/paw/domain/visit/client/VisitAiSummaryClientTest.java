package com.gaguraczi.paw.domain.visit.client;

import com.gaguraczi.paw.domain.rag.config.RagProperties;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class VisitAiSummaryClientTest {

    @Test
    void retriesWhenCharacterCountIsOutOfRangeThenSucceeds() {
        ClientFixture fixture = clientWithResponses(
                responseJson("짧음"),
                responseJson("가".repeat(1200))
        );

        var result = fixture.client().generate("전사문");

        assertThat(result.answer()).hasSize(1200);
        fixture.server().verify();
    }

    @Test
    void failsWhenRetryIsStillOutOfRange() {
        ClientFixture fixture = clientWithResponses(
                responseJson("짧음"),
                responseJson("아직도짧음")
        );

        assertThatThrownBy(() -> fixture.client().generate("전사문"))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
        fixture.server().verify();
    }

    @Test
    void returnsFileSearchSources() {
        ClientFixture fixture = clientWithResponses(responseJsonWithSource("가".repeat(1200)));

        var result = fixture.client().generate("전사문");

        assertThat(result.answer()).hasSize(1200);
        assertThat(result.sources()).hasSize(1);
        assertThat(result.sources().getFirst().sourceId()).isEqualTo("SRC-1");
        assertThat(result.sources().getFirst().disease()).isEqualTo("관절염");
        assertThat(result.sources().getFirst().score()).isEqualTo(0.91);
        fixture.server().verify();
    }

    private static ClientFixture clientWithResponses(String... bodies) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        for (String body : bodies) {
            server.expect(requestTo("https://api.openai.com/v1/responses"))
                    .andExpect(jsonPath("$.tool_choice.type").value("file_search"))
                    .andExpect(jsonPath("$.tools[0].vector_store_ids[0]").value("vs_test"))
                    .andExpect(jsonPath("$.include[0]").value("file_search_call.results"))
                    .andExpect(jsonPath("$.model").value("gpt-5.6-luna"))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        }

        VisitProperties visitProperties = new VisitProperties();
        RagProperties ragProperties = new RagProperties();
        ragProperties.setVectorStoreId("vs_test");
        ragProperties.setSearchTopK(8);
        RestClient restClient = builder.baseUrl("https://api.openai.com").build();
        return new ClientFixture(new VisitAiSummaryClient(restClient, visitProperties, ragProperties), server);
    }

    private record ClientFixture(VisitAiSummaryClient client, MockRestServiceServer server) {
    }

    private static String responseJson(String text) {
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"");
        return """
                {
                  "output": [
                    {
                      "type": "message",
                      "content": [
                        {"type": "output_text", "text": "%s"}
                      ]
                    }
                  ]
                }
                """.formatted(escaped);
    }

    private static String responseJsonWithSource(String text) {
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"");
        String snippet = "source_id: SRC-1 | chunk: 0 | type: QA\\n과목: 내과 | 생애주기: 노령견 | 질환: 관절염\\n[질문]\\n앞다리를 절어요";
        return """
                {
                  "output": [
                    {
                      "type": "file_search_call",
                      "results": [
                        {
                          "file_id": "file-1",
                          "filename": "내과_QA_000.md",
                          "score": 0.91,
                          "text": "%s"
                        }
                      ]
                    },
                    {
                      "type": "message",
                      "content": [
                        {"type": "output_text", "text": "%s"}
                      ]
                    }
                  ]
                }
                """.formatted(snippet, escaped);
    }
}

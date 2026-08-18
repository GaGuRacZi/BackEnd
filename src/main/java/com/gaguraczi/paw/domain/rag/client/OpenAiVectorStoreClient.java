package com.gaguraczi.paw.domain.rag.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.exception.code.RagErrorCode;
import com.gaguraczi.paw.domain.rag.support.OpenAiFileSearchResponseParser;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiVectorStoreClient {

    static final String INSTRUCTIONS = """
            너는 반려견 성장·질병 지식을 보호자에게 설명하는 보조다.
            file_search로 찾은 자료만 근거로 한국어 해요체로 답한다.
            자료에 없으면 모른다고 말하고, 진단이나 처방은 하지 말고 동물병원 상담을 권한다.
            """;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(90);

    private final RestClient restClient;

    @Autowired
    public OpenAiVectorStoreClient(
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.api-key}") String apiKey
    ) {
        this.restClient = restClientBuilder.clone()
                .requestFactory(ClientHttpRequestFactoryBuilder.detect()
                        .build(HttpClientSettings.defaults()
                                .withTimeouts(CONNECT_TIMEOUT, READ_TIMEOUT)))
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    OpenAiVectorStoreClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public RagAskResult ask(
            String vectorStoreId,
            String model,
            String reasoningEffort,
            String query,
            int maxNumResults
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("input", query);
        body.put("instructions", INSTRUCTIONS);
        body.put("max_output_tokens", 1500);
        body.put("tools", List.of(Map.of(
                "type", "file_search",
                "vector_store_ids", List.of(vectorStoreId),
                "max_num_results", maxNumResults
        )));
        body.put("include", List.of("file_search_call.results"));
        if (reasoningEffort != null && !reasoningEffort.isBlank()) {
            body.put("reasoning", Map.of("effort", reasoningEffort));
        }
        try {
            JsonNode response = restClient.post()
                    .uri("/v1/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            return OpenAiFileSearchResponseParser.parse(response);
        } catch (RestClientException e) {
            log.error("OpenAI file_search ask failed: {}", e.getMessage());
            throw GeneralException.of(RagErrorCode.RAG_SEARCH_FAILED, e);
        }
    }
}

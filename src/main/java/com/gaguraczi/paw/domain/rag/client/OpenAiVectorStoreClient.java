package com.gaguraczi.paw.domain.rag.client;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

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
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(45);
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 200L;
    private static final int BODY_EXCERPT_LIMIT = 500;

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

        RestClientResponseException lastResponse = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                JsonNode response = restClient.post()
                        .uri("/v1/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
                return OpenAiFileSearchResponseParser.parse(response);
            } catch (RestClientResponseException e) {
                lastResponse = e;
                int status = e.getStatusCode().value();
                log.error("OpenAI file_search failed status={} body={}", status, excerpt(e.getResponseBodyAsString()));
                if (!isRetryable(e.getStatusCode()) || attempt == MAX_ATTEMPTS) {
                    throw mapError(e);
                }
                backoff(attempt);
            } catch (RestClientException e) {
                log.error("OpenAI file_search ask failed: {}", e.getMessage());
                throw GeneralException.of(RagErrorCode.RAG_SEARCH_FAILED, e);
            }
        }
        throw mapError(lastResponse);
    }

    private static boolean isRetryable(HttpStatusCode status) {
        return status.value() == HttpStatus.TOO_MANY_REQUESTS.value() || status.is5xxServerError();
    }

    private static GeneralException mapError(RestClientResponseException e) {
        if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return GeneralException.of(RagErrorCode.RAG_RATE_LIMITED, e);
        }
        if (e.getStatusCode().is5xxServerError()) {
            return GeneralException.of(RagErrorCode.RAG_SEARCH_UNAVAILABLE, e);
        }
        return GeneralException.of(RagErrorCode.RAG_SEARCH_FAILED, e);
    }

    private static String excerpt(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String trimmed = body.strip();
        return trimmed.length() <= BODY_EXCERPT_LIMIT ? trimmed : trimmed.substring(0, BODY_EXCERPT_LIMIT);
    }

    private static void backoff(int attempt) {
        long delayMs = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw GeneralException.of(RagErrorCode.RAG_SEARCH_FAILED, e);
        }
    }
}

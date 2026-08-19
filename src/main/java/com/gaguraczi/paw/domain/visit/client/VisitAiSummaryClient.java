package com.gaguraczi.paw.domain.visit.client;

import com.gaguraczi.paw.domain.rag.config.RagProperties;
import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.support.OpenAiFileSearchResponseParser;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.support.VisitTextLimits;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class VisitAiSummaryClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(90);
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 200L;

    private final TimedRestClient timedRestClient;
    private final VisitProperties visitProperties;
    private final RagProperties ragProperties;

    @Autowired
    public VisitAiSummaryClient(
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.api-key}") String apiKey,
            VisitProperties visitProperties,
            RagProperties ragProperties
    ) {
        this.visitProperties = visitProperties;
        this.ragProperties = ragProperties;
        this.timedRestClient = new SharedTimedRestClient(restClientBuilder.clone()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey));
    }

    VisitAiSummaryClient(TimedRestClient timedRestClient, VisitProperties visitProperties, RagProperties ragProperties) {
        this.timedRestClient = timedRestClient;
        this.visitProperties = visitProperties;
        this.ragProperties = ragProperties;
    }

    public RagAskResult generate(String input) {
        Instant deadline = Instant.now().plus(READ_TIMEOUT);
        ensureBudget(deadline);
        RagAskResult first = callOnce(input, deadline);
        if (VisitTextLimits.inRange(first.answer(), visitProperties.getAiSummaryMinChars(), visitProperties.getAiSummaryMaxChars())) {
            return first;
        }
        String retryInput = input + "\n\n이전 초안 글자수가 범위를 벗어났다. 공백 포함 "
                + visitProperties.getAiSummaryMinChars() + "~" + visitProperties.getAiSummaryMaxChars()
                + "자로 다시 작성하라.\n초안:\n" + first.answer();
        ensureBudget(deadline);
        RagAskResult second = callOnce(retryInput, deadline);
        if (!VisitTextLimits.inRange(second.answer(), visitProperties.getAiSummaryMinChars(), visitProperties.getAiSummaryMaxChars())) {
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
        }
        return second;
    }

    private RagAskResult callOnce(String input, Instant deadline) {
        ensureBudget(deadline);
        String vectorStoreId = ragProperties.getVectorStoreId();
        if (vectorStoreId == null || vectorStoreId.isBlank()) {
            throw GeneralException.of(VisitErrorCode.VISIT_VECTOR_STORE_MISSING);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", visitProperties.getAiSummaryChatModel());
        body.put("input", input);
        body.put("instructions", instructions());
        body.put("max_output_tokens", 2500);
        body.put("tools", List.of(Map.of(
                "type", "file_search",
                "vector_store_ids", List.of(vectorStoreId),
                "max_num_results", ragProperties.getSearchTopK()
        )));
        body.put("tool_choice", Map.of("type", "file_search"));
        if (visitProperties.isAiSummaryIncludeSources()) {
            body.put("include", List.of("file_search_call.results"));
        }
        if (visitProperties.getReasoningEffort() != null && !visitProperties.getReasoningEffort().isBlank()) {
            body.put("reasoning", Map.of("effort", visitProperties.getReasoningEffort()));
        }

        RestClientResponseException lastResponse = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            ensureBudget(deadline);
            RestClient client = clientFor(remaining(deadline));
            try {
                JsonNode response = client.post()
                        .uri("/v1/responses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
                RagAskResult parsed = OpenAiFileSearchResponseParser.parse(response);
                if (parsed.answer() == null || parsed.answer().isBlank()) {
                    throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
                }
                int sourceCount = parsed.sources() == null ? 0 : parsed.sources().size();
                if (sourceCount == 0) {
                    log.warn("Visit AI summary file_search returned no sources; outputTypes={}", outputTypes(response));
                } else {
                    log.info("Visit AI summary file_search sources={}", sourceCount);
                }
                return new RagAskResult(parsed.answer().trim(), parsed.sources());
            } catch (RestClientResponseException e) {
                lastResponse = e;
                log.error("Visit AI summary failed status={}", e.getStatusCode().value());
                if (!isRetryable(e.getStatusCode()) || attempt == MAX_ATTEMPTS) {
                    throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED, e);
                }
                ensureBudget(deadline);
                backoff(attempt, deadline);
            } catch (RestClientException e) {
                throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED, e);
            } finally {
                timedRestClient.release();
            }
        }
        throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED, lastResponse);
    }

    private String instructions() {
        return """
                너는 반려동물 진료 내용을 보호자에게 설명하는 보조다.
                요약 전에 반드시 file_search로 진료 주제(치과, 마취, 스케일링, 처방 등) 관련 지식을 검색한다.
                전사문·처방 약물·file_search 자료만 근거로 한국어 해요체 마크다운을 작성한다.
                전사에 없는 진단·처방·처치를 지어내지 마라. file_search는 보호자 설명·주의·관리 안내에만 보탠다.
                줄바꿈과 제목/불릿 마크다운을 사용한다.
                본문 길이는 공백 포함 %d자 이상 %d자 이하다.
                마지막에 이 요약은 진료 기록을 돕기 위한 것이며 수의사 진단을 대신하지 않는다는 한 줄을 넣어라.
                """.formatted(visitProperties.getAiSummaryMinChars(), visitProperties.getAiSummaryMaxChars());
    }

    private RestClient clientFor(Duration remaining) {
        Duration readTimeout = remaining.compareTo(READ_TIMEOUT) < 0 ? remaining : READ_TIMEOUT;
        if (readTimeout.isZero() || readTimeout.isNegative()) {
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
        }
        return timedRestClient.forRemaining(readTimeout);
    }

    private static Duration remaining(Instant deadline) {
        Duration remaining = Duration.between(Instant.now(), deadline);
        if (remaining.isZero() || remaining.isNegative()) {
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
        }
        return remaining;
    }

    private static void ensureBudget(Instant deadline) {
        if (!Instant.now().isBefore(deadline)) {
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
        }
    }

    private static String outputTypes(JsonNode response) {
        if (response == null) {
            return "";
        }
        JsonNode output = response.path("output");
        if (!output.isArray()) {
            return "";
        }
        List<String> types = new ArrayList<>();
        for (JsonNode item : output) {
            types.add(item.path("type").asString("unknown"));
        }
        return String.join(",", types);
    }

    private static boolean isRetryable(org.springframework.http.HttpStatusCode status) {
        return status.value() == HttpStatus.TOO_MANY_REQUESTS.value() || status.is5xxServerError();
    }

    private static void backoff(int attempt, Instant deadline) {
        long delayMs = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
        Duration remaining = remaining(deadline);
        if (remaining.toMillis() < delayMs) {
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED, e);
        }
    }

    @FunctionalInterface
    interface TimedRestClient {
        RestClient forRemaining(Duration remaining);

        default void release() {
        }
    }

    private static final class SharedTimedRestClient implements TimedRestClient {
        private final RestClient restClient;
        private final ThreadLocal<Duration> readTimeout = new ThreadLocal<>();

        SharedTimedRestClient(RestClient.Builder baseBuilder) {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT)
                    .build();
            ClientHttpRequestFactory factory = (uri, method) -> {
                Duration timeout = Optional.ofNullable(readTimeout.get()).orElse(READ_TIMEOUT);
                JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
                requestFactory.setReadTimeout(timeout);
                return requestFactory.createRequest(uri, method);
            };
            this.restClient = baseBuilder.requestFactory(factory).build();
        }

        @Override
        public RestClient forRemaining(Duration remaining) {
            readTimeout.set(remaining);
            return restClient;
        }

        @Override
        public void release() {
            readTimeout.remove();
        }
    }
}

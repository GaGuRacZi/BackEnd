package com.gaguraczi.paw.domain.visit.client;

import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

import java.time.Duration;

@Slf4j
@Component
public class OpenAiSttClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(900);

    private final RestClient restClient;
    private final VisitProperties visitProperties;

    @Autowired
    public OpenAiSttClient(
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.api-key}") String apiKey,
            VisitProperties visitProperties
    ) {
        this.visitProperties = visitProperties;
        this.restClient = restClientBuilder.clone()
                .requestFactory(ClientHttpRequestFactoryBuilder.detect()
                        .build(HttpClientSettings.defaults()
                                .withTimeouts(CONNECT_TIMEOUT, READ_TIMEOUT)))
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    OpenAiSttClient(RestClient restClient, VisitProperties visitProperties) {
        this.restClient = restClient;
        this.visitProperties = visitProperties;
    }

    public DiarizedTranscript transcribe(byte[] audio, String filename, String contentType) {
        if (audio == null || audio.length == 0) {
            throw GeneralException.of(VisitErrorCode.VISIT_STT_FAILED);
        }
        String safeName = (filename == null || filename.isBlank()) ? "visit-audio.m4a" : filename;
        ByteArrayResource fileResource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return safeName;
            }
        };
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", visitProperties.getSttModel());
        body.add("response_format", "diarized_json");
        body.add("chunking_strategy", "auto");
        body.add("file", fileResource);

        try {
            JsonNode response = restClient.post()
                    .uri("/v1/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            DiarizedTranscript parsed = OpenAiSttResponseParser.parse(response);
            if (parsed.segments().isEmpty() && (parsed.text() == null || parsed.text().isBlank())) {
                throw GeneralException.of(VisitErrorCode.VISIT_STT_FAILED);
            }
            return parsed;
        } catch (GeneralException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.error("OpenAI STT failed status={} body={}", e.getStatusCode().value(), excerpt(e.getResponseBodyAsString()));
            if (e.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()
                    || e.getStatusCode().is5xxServerError()) {
                throw GeneralException.of(VisitErrorCode.VISIT_STT_FAILED, e);
            }
            throw GeneralException.of(VisitErrorCode.VISIT_STT_FAILED, e);
        } catch (RestClientException e) {
            log.error("OpenAI STT request failed: {}", e.getMessage());
            throw GeneralException.of(VisitErrorCode.VISIT_STT_FAILED, e);
        }
    }

    private static String excerpt(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String trimmed = body.strip();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }
}

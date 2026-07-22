package com.gaguraczi.paw.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.global.config.properties.KakaoProperties;
import java.time.Duration;
import lombok.Getter;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoApiClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final KakaoProperties kakaoProperties;
    private final RestClient restClient;

    public KakaoApiClient(KakaoProperties kakaoProperties, RestClient.Builder restClientBuilder) {
        this.kakaoProperties = kakaoProperties;
        this.restClient = restClientBuilder
                .requestFactory(ClientHttpRequestFactoryBuilder.detect()
                        .build(HttpClientSettings.defaults()
                                .withTimeouts(CONNECT_TIMEOUT, READ_TIMEOUT)))
                .build();
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = restClient
                    .get()
                    .uri(kakaoProperties.getUserInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (response == null || response.getId() == null) {
                throw AuthException.of(AuthErrorCode.KAKAO_LOGIN_UNAUTHORIZED);
            }

            String email = null;
            if (response.getKakaoAccount() != null) {
                email = response.getKakaoAccount().getEmail();
            }

            return new KakaoUserInfo(String.valueOf(response.getId()), email);
        } catch (AuthException e) {
            throw e;
        } catch (org.springframework.web.client.RestClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                throw AuthException.of(AuthErrorCode.KAKAO_LOGIN_UNAUTHORIZED);
            }
            throw AuthException.of(AuthErrorCode.KAKAO_API_FAILED);
        } catch (RestClientException e) {
            throw AuthException.of(AuthErrorCode.KAKAO_API_FAILED);
        }
    }

    public record KakaoUserInfo(String providerId, String email) {
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KakaoUserResponse {
        private Long id;

        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KakaoAccount {
        private String email;
    }
}

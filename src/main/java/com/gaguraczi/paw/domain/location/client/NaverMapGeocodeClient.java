package com.gaguraczi.paw.domain.location.client;

import com.gaguraczi.paw.domain.location.dto.res.NaverGeocodeRes;
import com.gaguraczi.paw.domain.location.exception.code.LocationErrorCode;
import com.gaguraczi.paw.global.config.properties.NaverMapProperties;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NaverMapGeocodeClient {

    private static final String PATH = "/map-geocode/v2/geocode";

    private final RestClient naverMapRestClient;
    private final NaverMapProperties naverMapProperties;

    public NaverMapGeocodeClient(
            @Qualifier("naverMapRestClient") RestClient naverMapRestClient,
            NaverMapProperties naverMapProperties
    ) {
        this.naverMapRestClient = naverMapRestClient;
        this.naverMapProperties = naverMapProperties;
    }

    public NaverGeocodeRes geocode(String query) {
        if (query == null || query.isBlank()) {
            throw GeneralException.of(LocationErrorCode.LOCATION_INVALID_REQUEST);
        }
        validateApiKeys();
        try {
            NaverGeocodeRes response = naverMapRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(PATH)
                            .queryParam("query", query)
                            .queryParam("language", "kor")
                            .queryParam("page", 1)
                            .queryParam("count", 1)
                            .build())
                    .header("x-ncp-apigw-api-key-id", naverMapProperties.clientId())
                    .header("x-ncp-apigw-api-key", naverMapProperties.clientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(NaverGeocodeRes.class);

            if (response == null) {
                throw GeneralException.of(LocationErrorCode.LOCATION_RESPONSE_EMPTY);
            }
            if (response.status() == null || !"OK".equalsIgnoreCase(response.status())) {
                throw GeneralException.of(LocationErrorCode.LOCATION_GEOCODE_API_FAILED);
            }
            return response;
        } catch (RestClientException e) {
            throw GeneralException.of(LocationErrorCode.LOCATION_GEOCODE_API_FAILED, e);
        }
    }

    private void validateApiKeys() {
        if (naverMapProperties.clientId() == null || naverMapProperties.clientId().isBlank()
                || naverMapProperties.clientSecret() == null || naverMapProperties.clientSecret().isBlank()) {
            throw GeneralException.of(LocationErrorCode.LOCATION_API_KEY_MISSING);
        }
    }
}

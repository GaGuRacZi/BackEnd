package com.gaguraczi.paw.domain.location.client;

import com.gaguraczi.paw.domain.location.dto.res.NaverReverseGeocodeRes;
import com.gaguraczi.paw.domain.location.exception.code.LocationErrorCode;
import com.gaguraczi.paw.global.config.properties.NaverMapProperties;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@Component
public class NaverMapReverseGeocodeClient {

    private static final String PATH = "/map-reversegeocode/v2/gc";
    private static final String ORDERS = "legalcode,admcode,addr,roadaddr";

    private final RestClient naverMapRestClient;
    private final NaverMapProperties naverMapProperties;

    public NaverMapReverseGeocodeClient(
            @Qualifier("naverMapRestClient") RestClient naverMapRestClient,
            NaverMapProperties naverMapProperties
    ) {
        this.naverMapRestClient = naverMapRestClient;
        this.naverMapProperties = naverMapProperties;
    }

    public NaverReverseGeocodeRes reverseGeocode(double longitude, double latitude) {
        validateApiKeys();
        try {
            NaverReverseGeocodeRes response = naverMapRestClient.get()
                    .uri(uriBuilder -> buildUri(uriBuilder, longitude, latitude))
                    .header("x-ncp-apigw-api-key-id", naverMapProperties.getClientId())
                    .header("x-ncp-apigw-api-key", naverMapProperties.getClientSecret())
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(NaverReverseGeocodeRes.class);

            if (response == null || response.status() == null) {
                throw GeneralException.of(LocationErrorCode.LOCATION_RESPONSE_EMPTY);
            }
            int statusCode = response.status().code();
            if (statusCode != 0 && statusCode != 3) {
                throw GeneralException.of(LocationErrorCode.LOCATION_REVERSE_GEOCODE_API_FAILED);
            }
            return new NaverReverseGeocodeRes(
                    response.status(),
                    Objects.requireNonNullElse(response.results(), List.of())
            );
        } catch (RestClientException e) {
            throw GeneralException.of(LocationErrorCode.LOCATION_REVERSE_GEOCODE_API_FAILED);
        }
    }

    private URI buildUri(UriBuilder uriBuilder, double longitude, double latitude) {
        return uriBuilder
                .path(PATH)
                .queryParam("request", "coordsToaddr")
                .queryParam("coords", longitude + "," + latitude)
                .queryParam("output", "json")
                .queryParam("orders", ORDERS)
                .build();
    }

    private void validateApiKeys() {
        if (naverMapProperties.getClientId() == null || naverMapProperties.getClientId().isBlank()
                || naverMapProperties.getClientSecret() == null || naverMapProperties.getClientSecret().isBlank()) {
            throw GeneralException.of(LocationErrorCode.LOCATION_API_KEY_MISSING);
        }
    }
}

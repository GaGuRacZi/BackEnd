package com.gaguraczi.paw.domain.location.service;

import com.gaguraczi.paw.domain.location.client.NaverMapReverseGeocodeClient;
import com.gaguraczi.paw.domain.location.dto.res.LegalDistrictAddressRes;
import com.gaguraczi.paw.domain.location.dto.res.NaverReverseGeocodeRes;
import com.gaguraczi.paw.domain.location.exception.code.LocationErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NaverMapService {

    private final NaverMapReverseGeocodeClient reverseGeocodeClient;

    public LegalDistrictAddressRes resolveLegalDistrictCodeAndAddress(double longitude, double latitude) {
        NaverReverseGeocodeRes result = reverseGeocodeClient.reverseGeocode(longitude, latitude);
        String legalDistrictCode = result.legalDistrictCode()
                .orElseThrow(() -> GeneralException.of(LocationErrorCode.LOCATION_LEGAL_DISTRICT_CODE_NOT_FOUND));
        String address = resolveDisplayAddress(result);
        return new LegalDistrictAddressRes(legalDistrictCode, address);
    }

    public String resolveRoadAddress(double longitude, double latitude) {
        NaverReverseGeocodeRes result = reverseGeocodeClient.reverseGeocode(longitude, latitude);
        return resolveDisplayAddress(result);
    }

    private String resolveDisplayAddress(NaverReverseGeocodeRes result) {
        return result.results().stream()
                .filter(item -> "roadaddr".equalsIgnoreCase(item.name()))
                .map(this::buildRoadAddress)
                .filter(address -> address != null && !address.isBlank())
                .findFirst()
                .or(() -> resolveJibunAddress(result))
                .or(() -> resolveRegionAddress(result))
                .orElseThrow(() -> GeneralException.of(LocationErrorCode.LOCATION_ADDRESS_NOT_FOUND));
    }

    private String buildRoadAddress(NaverReverseGeocodeRes.ResultItem item) {
        NaverReverseGeocodeRes.Land land = item.land();
        if (land == null || land.name() == null || land.name().isBlank()) {
            return null;
        }
        String regionPrefix = buildRegionPrefix(item.region());
        String road = (land.name() + " " + formatLandNumber(land)).trim();
        if (road.isBlank()) {
            return null;
        }
        return (regionPrefix + " " + road).trim().replaceAll("\\s+", " ");
    }

    private Optional<String> resolveJibunAddress(NaverReverseGeocodeRes result) {
        return result.results().stream()
                .filter(item -> "addr".equalsIgnoreCase(item.name()))
                .map(item -> {
                    NaverReverseGeocodeRes.Land land = item.land();
                    if (land == null) {
                        return null;
                    }
                    String regionPrefix = buildRegionPrefix(item.region());
                    String jibun = formatLandNumber(land).trim();
                    if (jibun.isBlank()) {
                        return null;
                    }
                    return (regionPrefix + " " + jibun).trim().replaceAll("\\s+", " ");
                })
                .filter(jibun -> jibun != null && !jibun.isBlank())
                .findFirst();
    }

    private static String formatLandNumber(NaverReverseGeocodeRes.Land land) {
        String number1 = land.number1() == null ? "" : land.number1();
        String number2 = (land.number2() == null || land.number2().isBlank()) ? "" : "-" + land.number2();
        return number1 + number2;
    }

    private Optional<String> resolveRegionAddress(NaverReverseGeocodeRes result) {
        return result.results().stream()
                .sorted(Comparator.comparingInt(item -> {
                    if ("admcode".equalsIgnoreCase(item.name())) return 0;
                    if ("legalcode".equalsIgnoreCase(item.name())) return 1;
                    return 2;
                }))
                .map(NaverReverseGeocodeRes.ResultItem::region)
                .filter(Objects::nonNull)
                .map(this::buildRegionPrefix)
                .filter(regionAddress -> !regionAddress.isBlank())
                .findFirst();
    }

    private String buildRegionPrefix(NaverReverseGeocodeRes.Region region) {
        if (region == null) {
            return "";
        }
        String area1 = region.area1() == null || region.area1().name() == null ? "" : region.area1().name();
        String area2 = region.area2() == null || region.area2().name() == null ? "" : region.area2().name();
        String area3 = region.area3() == null || region.area3().name() == null ? "" : region.area3().name();
        String area4 = region.area4() == null || region.area4().name() == null ? "" : region.area4().name();
        return (area1 + " " + area2 + " " + area3 + " " + area4).trim().replaceAll("\\s+", " ");
    }
}

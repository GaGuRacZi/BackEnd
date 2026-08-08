package com.gaguraczi.paw.domain.location.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverReverseGeocodeRes(
        Status status,
        List<ResultItem> results
) {
    public Optional<String> legalDistrictCode() {
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }
        return results.stream()
                .filter(item -> "legalcode".equalsIgnoreCase(item.name()))
                .map(ResultItem::code)
                .filter(Objects::nonNull)
                .filter(code -> code.type() == null || "L".equalsIgnoreCase(code.type()))
                .map(Code::id)
                .filter(id -> id != null && !id.isBlank())
                .findFirst();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(int code, String name, String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultItem(String name, Code code, Region region, Land land) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Code(String id, String type, String mappingId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Region(Area area0, Area area1, Area area2, Area area3, Area area4) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Area(String name, String alias) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Land(String type, String name, String number1, String number2) {
    }
}

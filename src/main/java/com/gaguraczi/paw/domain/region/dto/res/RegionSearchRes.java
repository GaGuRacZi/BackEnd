package com.gaguraczi.paw.domain.region.dto.res;

import java.util.List;

public record RegionSearchRes(
        String code,
        String name,
        List<String> dongPreview
) {
    public static RegionSearchRes of(String code, String name, List<String> dongPreview) {
        return new RegionSearchRes(code, name, dongPreview);
    }
}

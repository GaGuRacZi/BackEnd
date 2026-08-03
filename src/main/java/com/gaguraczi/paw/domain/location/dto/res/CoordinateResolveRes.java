package com.gaguraczi.paw.domain.location.dto.res;

import lombok.Builder;

@Builder
public record CoordinateResolveRes(
        String regionCode,
        String regionName,
        String address,
        Double latitude,
        Double longitude
) {
}

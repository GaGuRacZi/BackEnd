package com.gaguraczi.paw.domain.location.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoordinateResolveRes {

    private final String regionCode;
    private final String regionName;
    private final String address;
    private final Double latitude;
    private final Double longitude;
}

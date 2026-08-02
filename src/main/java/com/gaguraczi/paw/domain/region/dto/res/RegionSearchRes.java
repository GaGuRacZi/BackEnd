package com.gaguraczi.paw.domain.region.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RegionSearchRes {

    private final String code;
    private final String name;
    private final List<String> dongPreview;
}

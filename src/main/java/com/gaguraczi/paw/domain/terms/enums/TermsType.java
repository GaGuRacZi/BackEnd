package com.gaguraczi.paw.domain.terms.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "약관 타입")
public enum TermsType {
    AGE_OVER_14,
    TERMS_OF_SERVICE,
    PRIVACY,
    PROFILE_EXTRA,
    MARKETING_PUSH,
    LOCATION_SERVICE
}

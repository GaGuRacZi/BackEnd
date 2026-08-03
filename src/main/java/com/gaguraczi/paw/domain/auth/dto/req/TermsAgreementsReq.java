package com.gaguraczi.paw.domain.auth.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.EnumMap;
import java.util.Map;

@Schema(description = "약관 동의 (key는 TermsType enum 이름)")
public record TermsAgreementsReq(
        @NotNull(message = "만 14세 이상 확인 동의는 필수입니다.")
        @JsonProperty("AGE_OVER_14")
        @Schema(name = "AGE_OVER_14", description = "만 14세 이상 확인", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean ageOver14,

        @NotNull(message = "서비스 이용약관 동의는 필수입니다.")
        @JsonProperty("TERMS_OF_SERVICE")
        @Schema(name = "TERMS_OF_SERVICE", description = "서비스 이용약관", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean termsOfService,

        @NotNull(message = "개인정보 수집·이용 동의는 필수입니다.")
        @JsonProperty("PRIVACY")
        @Schema(name = "PRIVACY", description = "개인정보 수집·이용 동의", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean privacy,

        @NotNull(message = "프로필 추가정보 수집·이용 동의는 필수입니다.")
        @JsonProperty("PROFILE_EXTRA")
        @Schema(name = "PROFILE_EXTRA", description = "프로필 추가정보 수집·이용 동의", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean profileExtra,

        @NotNull(message = "마케팅 정보 수신 동의 여부는 필수입니다.")
        @JsonProperty("MARKETING_PUSH")
        @Schema(name = "MARKETING_PUSH", description = "마케팅 정보 수신 동의(앱 푸시)", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean marketingPush,

        @NotNull(message = "위치기반 서비스 이용약관 동의 여부는 필수입니다.")
        @JsonProperty("LOCATION_SERVICE")
        @Schema(name = "LOCATION_SERVICE", description = "위치기반 서비스 이용약관", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean locationService
) {
    public Map<TermsType, Boolean> toMap() {
        Map<TermsType, Boolean> map = new EnumMap<>(TermsType.class);
        map.put(TermsType.AGE_OVER_14, ageOver14);
        map.put(TermsType.TERMS_OF_SERVICE, termsOfService);
        map.put(TermsType.PRIVACY, privacy);
        map.put(TermsType.PROFILE_EXTRA, profileExtra);
        map.put(TermsType.MARKETING_PUSH, marketingPush);
        map.put(TermsType.LOCATION_SERVICE, locationService);
        return map;
    }
}

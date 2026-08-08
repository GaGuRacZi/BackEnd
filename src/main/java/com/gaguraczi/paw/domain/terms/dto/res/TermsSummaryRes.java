package com.gaguraczi.paw.domain.terms.dto.res;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "약관 요약 응답")
public class TermsSummaryRes {

    @Schema(description = "약관 타입", example = "TERMS_OF_SERVICE")
    private final TermsType type;

    @Schema(description = "약관 제목", example = "서비스 이용약관")
    private final String title;

    @Schema(description = "약관 버전", example = "1.0")
    private final String version;

    @Schema(description = "필수 동의 여부", example = "true")
    private final boolean required;

    @Schema(description = "시행일", example = "2025-01-01")
    private final LocalDate effectiveAt;

    public static TermsSummaryRes from(Terms terms) {
        return TermsSummaryRes.builder()
                .type(terms.getType())
                .title(terms.getTitle())
                .version(terms.getVersion())
                .required(terms.isRequired())
                .effectiveAt(terms.getEffectiveAt())
                .build();
    }
}

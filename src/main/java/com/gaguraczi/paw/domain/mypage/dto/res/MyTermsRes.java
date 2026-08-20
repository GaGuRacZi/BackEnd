package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "약관 요약 + 내 동의 여부. 본문은 GET /terms/{type}")
public record MyTermsRes(
        @Schema(description = "약관 타입", example = "TERMS_OF_SERVICE")
        TermsType type,
        @Schema(description = "약관 제목", example = "서비스 이용약관")
        String title,
        @Schema(description = "버전", example = "1.0")
        String version,
        @Schema(description = "필수 약관 여부", example = "true")
        boolean required,
        @Schema(description = "시행일", example = "2025-01-01")
        LocalDate effectiveAt,
        @Schema(description = "해당 type+version에 동의했는지", example = "true")
        boolean agreed
) {
    public static MyTermsRes of(Terms terms, boolean agreed) {
        return new MyTermsRes(
                terms.getType(),
                terms.getTitle(),
                terms.getVersion(),
                terms.isRequired(),
                terms.getEffectiveAt(),
                agreed
        );
    }
}

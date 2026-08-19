package com.gaguraczi.paw.domain.terms.dto.res;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "약관 상세 응답")
public record TermsDetailRes(
        @Schema(description = "약관 타입", example = "TERMS_OF_SERVICE")
        TermsType type,
        @Schema(description = "약관 제목", example = "서비스 이용약관")
        String title,
        @Schema(description = "약관 본문", example = "제1조 (목적)\n본 약관은 ...")
        String content,
        @Schema(description = "약관 버전", example = "1.0")
        String version,
        @Schema(description = "필수 동의 여부", example = "true")
        boolean required,
        @Schema(description = "시행일", example = "2025-01-01")
        LocalDate effectiveAt
) {
    public static TermsDetailRes from(Terms terms) {
        return new TermsDetailRes(
                terms.getType(),
                terms.getTitle(),
                terms.getContent(),
                terms.getVersion(),
                terms.isRequired(),
                terms.getEffectiveAt()
        );
    }
}

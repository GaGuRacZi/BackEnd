package com.gaguraczi.paw.domain.terms.dto.res;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;

import java.time.LocalDate;

public record TermsDetailRes(
        TermsType type,
        String title,
        String content,
        String version,
        boolean required,
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

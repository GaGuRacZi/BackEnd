package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;

import java.time.LocalDate;

public record MyTermsRes(
        TermsType type,
        String title,
        String version,
        boolean required,
        LocalDate effectiveAt,
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

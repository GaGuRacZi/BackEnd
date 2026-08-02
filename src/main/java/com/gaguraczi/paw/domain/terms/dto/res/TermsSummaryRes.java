package com.gaguraczi.paw.domain.terms.dto.res;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TermsSummaryRes {

    private final TermsType type;
    private final String title;
    private final String version;
    private final boolean required;
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

package com.gaguraczi.paw.domain.terms.dto.res;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TermsDetailRes {

    private final TermsType type;
    private final String title;
    private final String content;
    private final String version;
    private final boolean required;
    private final LocalDate effectiveAt;

    public static TermsDetailRes from(Terms terms) {
        return TermsDetailRes.builder()
                .type(terms.getType())
                .title(terms.getTitle())
                .content(terms.getContent())
                .version(terms.getVersion())
                .required(terms.isRequired())
                .effectiveAt(terms.getEffectiveAt())
                .build();
    }
}

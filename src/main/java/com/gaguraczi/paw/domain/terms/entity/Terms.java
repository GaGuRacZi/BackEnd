package com.gaguraczi.paw.domain.terms.entity;

import com.gaguraczi.paw.domain.terms.enums.TermsType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "terms",
        uniqueConstraints = @UniqueConstraint(name = "uk_terms_type_version", columnNames = {"type", "version"})
)
public class Terms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_id")
    private Long termsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 40, nullable = false)
    private TermsType type;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "version", length = 20, nullable = false)
    private String version;

    @Builder.Default
    @Column(name = "required", nullable = false)
    private boolean required = true;

    @Column(name = "effective_at", nullable = false)
    private LocalDate effectiveAt;
}

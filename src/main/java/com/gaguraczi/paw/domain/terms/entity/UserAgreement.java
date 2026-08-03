package com.gaguraczi.paw.domain.terms.entity;

import com.gaguraczi.paw.domain.terms.enums.TermsType;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_agreement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_agreement_user_type_version",
                columnNames = {"uid", "terms_type", "terms_version"}
        )
)
public class UserAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_agreement_id")
    private Long userAgreementId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", length = 40, nullable = false)
    private TermsType termsType;

    @Column(name = "terms_version", length = 20, nullable = false)
    private String termsVersion;

    @Builder.Default
    @Column(name = "agreed", nullable = false)
    private boolean agreed = false;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;
}

package com.gaguraczi.paw.domain.auth.entity;

import com.gaguraczi.paw.domain.auth.enums.SocialType;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "oauth",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_oauth_provider_id_social_type",
                columnNames = {"provider_id", "social_type"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class OAuth extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long oauth_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false)
    private SocialType providerType;

    @Column(name = "password")
    private String password;

    /** 제공자별 이메일. 중복 허용 (unique 아님). 계정 식별은 users.email */
    @Column(name = "email")
    private String email;


}

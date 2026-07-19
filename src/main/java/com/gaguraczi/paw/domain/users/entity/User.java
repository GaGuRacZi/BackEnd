package com.gaguraczi.paw.domain.users.entity;

import com.gaguraczi.paw.domain.users.enums.RoleType;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "uid", nullable = false, length = 50)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uid;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "intro", columnDefinition = "TEXT")
    private String intro;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "user_point", columnDefinition = "geometry(Point,4326)")
    private Point userPoint;

    /** 계정 식별용. 유저 간 중복 불가 */
    @Column(name = "email", unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role")
    private RoleType role = RoleType.USER;

    @Column(name = "push_token")
    private String pushToken;

    @Builder.Default
    @Column(name = "coin")
    private Integer coin = 0;

    @Builder.Default
    @Column(name = "subscribe")
    @Enumerated(EnumType.STRING)
    private SubscribeType subscribe = SubscribeType.BASIC;

    @Builder.Default
    @Column(name = "is_new")
    private boolean isNew = true;

    public void completeOnboarding(String name, String nickname, String intro) {
        this.name = name;
        this.nickname = nickname;
        this.intro = intro;
        this.isNew = false;
    }

    public void updateEmail(String email) {
        this.email = email;
    }
}

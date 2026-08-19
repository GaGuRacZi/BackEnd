package com.gaguraczi.paw.domain.users.entity;

import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.users.enums.RoleType;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
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

    @Column(name = "profile_s3_key", length = 255, unique = true)
    private String profileS3Key;

    @Column(name = "profile_url", columnDefinition = "TEXT")
    private String profileUrl;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "user_point", columnDefinition = "geometry(Point,4326)")
    private Point userPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code")
    private LegalRegion region;

    /** 위치 인증 시 해석된 표시용 주소 (getMyLocation에서 재사용) */
    @Column(name = "location_address", length = 500)
    private String locationAddress;

    /** 계정 식별용. 유저 간 중복 불가 */
    @Column(name = "email", unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false)
    private RoleType role = RoleType.USER;

    @Column(name = "push_token")
    private String pushToken;

    @Builder.Default
    @ColumnDefault("10")
    @Column(name = "coin", nullable = false)
    private Integer coin = 10;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "used_coin", nullable = false)
    private Integer usedCoin = 0;

    @Builder.Default
    @Column(name = "subscribe")
    @Enumerated(EnumType.STRING)
    private SubscribeType subscribe = SubscribeType.BASIC;

    @Builder.Default
    @Column(name = "is_new")
    private boolean isNew = true;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void completeOnboarding(
            String name,
            String nickname,
            String intro,
            Point userPoint,
            LegalRegion region
    ) {
        this.name = name;
        this.nickname = nickname;
        this.intro = intro;
        this.userPoint = userPoint;
        this.region = region;
        this.isNew = false;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateProfile(String name, String nickname, String intro) {
        if (name != null) {
            this.name = name;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (intro != null) {
            this.intro = intro.isBlank() ? null : intro;
        }
    }

    public void updateProfileImage(String profileS3Key, String profileUrl) {
        this.profileS3Key = profileS3Key;
        this.profileUrl = profileUrl;
    }

    public void updateLocation(Point userPoint, LegalRegion region, String locationAddress) {
        this.userPoint = userPoint;
        this.region = region;
        if (locationAddress != null && !locationAddress.isBlank()) {
            this.locationAddress = locationAddress;
        }
    }

    /** Soft delete: 개인식별정보는 익명화하고 이미 생성된 콘텐츠(커뮤니티 글/댓글 등)는 보존한다. */
    public void withdraw() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.name = null;
        this.nickname = "탈퇴한 사용자";
        this.intro = null;
        this.profileS3Key = null;
        this.profileUrl = null;
        this.email = "withdrawn-" + this.uid + "@paw.local";
        this.pushToken = null;
    }

    public int coinBalance() {
        return coin == null ? 0 : coin;
    }

    public int usedCoinBalance() {
        return usedCoin == null ? 0 : usedCoin;
    }

    public void deductCoin(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        int current = coinBalance();
        if (current < amount) {
            throw new IllegalStateException("insufficient coin");
        }
        this.coin = current - amount;
        this.usedCoin = usedCoinBalance() + amount;
    }

    public void refundCoin(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.coin = coinBalance() + amount;
        this.usedCoin = Math.max(0, usedCoinBalance() - amount);
    }
}

package com.gaguraczi.paw.domain.notification.entity;

import com.gaguraczi.paw.domain.notification.enums.DevicePlatform;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 멀티 디바이스 지원을 위해 uid : token = 1 : N. */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "device_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_device_token_token", columnNames = "token"),
        indexes = @Index(name = "idx_device_token_uid", columnList = "uid")
)
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    private Long deviceTokenId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, length = 255, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 20)
    private DevicePlatform platform;

    public void reassignTo(User newOwner) {
        this.user = newOwner;
    }

    public void changePlatform(DevicePlatform platform) {
        this.platform = platform;
    }
}

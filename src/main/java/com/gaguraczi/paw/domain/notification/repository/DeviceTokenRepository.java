package com.gaguraczi.paw.domain.notification.repository;

import com.gaguraczi.paw.domain.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUser_Uid(UUID uid);

    void deleteByTokenAndUser_Uid(String token, UUID uid);

    void deleteByUser_Uid(UUID uid);
}

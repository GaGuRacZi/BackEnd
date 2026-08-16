package com.gaguraczi.paw.domain.mypage.repository;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByUser(User user);
}

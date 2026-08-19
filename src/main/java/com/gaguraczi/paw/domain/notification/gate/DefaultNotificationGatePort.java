package com.gaguraczi.paw.domain.notification.gate;

import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.users.entity.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** 임시 구현(항상 허용). 실제 NotificationGatePort 구현체가 추가되면 자동으로 대체된다. */
@Component
@ConditionalOnMissingBean(NotificationGatePort.class)
public class DefaultNotificationGatePort implements NotificationGatePort {

    @Override
    public boolean allow(User receiver, NotificationCategory category) {
        return true;
    }
}

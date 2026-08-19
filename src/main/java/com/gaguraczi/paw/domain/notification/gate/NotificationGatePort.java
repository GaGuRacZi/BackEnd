package com.gaguraczi.paw.domain.notification.gate;

import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.users.entity.User;

/** 발송 전 게이팅(카테고리 on/off + 방해금지 시간) 판단. */
public interface NotificationGatePort {

    boolean allow(User receiver, NotificationCategory category);
}

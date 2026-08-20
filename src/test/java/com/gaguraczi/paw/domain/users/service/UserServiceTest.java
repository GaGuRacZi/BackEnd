package com.gaguraczi.paw.domain.users.service;

import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private S3Utils s3Utils;

    @InjectMocks
    private UserService userService;

    @Test
    void 푸시토큰을_저장하고_빈값이면_해제한다() {
        User user = User.builder().uid(UUID.randomUUID()).build();
        when(securityUtils.currentUser()).thenReturn(user);

        userService.updatePushToken("  device-token  ");
        assertThat(user.getPushToken()).isEqualTo("device-token");

        userService.updatePushToken(" ");
        assertThat(user.getPushToken()).isNull();
    }
}

package com.gaguraczi.paw.global.security;

import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUtils securityUtils;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 탈퇴_계정은_currentUid도_거부한다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).build();
        user.withdraw(LocalDateTime.of(2026, 8, 21, 5, 0));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(uid.toString(), null)
        );
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> securityUtils.currentUid())
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(AuthErrorCode.LOGIN_LINK_400);
    }
}

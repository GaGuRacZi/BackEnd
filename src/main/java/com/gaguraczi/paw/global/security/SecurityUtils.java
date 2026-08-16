package com.gaguraczi.paw.global.security;

import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public UUID currentUid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
        try {
            return UUID.fromString(authentication.getPrincipal().toString());
        } catch (IllegalArgumentException e) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
    }

    public User currentUser() {
        User user = userRepository.findById(currentUid())
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));
        if (user.isDeleted()) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
        return user;
    }
}

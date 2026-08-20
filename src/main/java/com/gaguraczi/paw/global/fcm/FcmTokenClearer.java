package com.gaguraczi.paw.global.fcm;

import com.gaguraczi.paw.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenClearer {

    private final UserRepository userRepository;

    @Transactional
    public void clearByToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        userRepository.clearPushToken(token.trim());
    }
}

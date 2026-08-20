package com.gaguraczi.paw.domain.users.service;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.RoleType;
import com.gaguraczi.paw.domain.users.exception.code.UserErrorCode;
import com.gaguraczi.paw.domain.users.repository.AdminUserHardDeleteJdbcRepository;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.domain.walk.redis.WalkInProgressRedisStore;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.redis.RefreshTokenRedisStore;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Utils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserHardDeleteService {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final AdminUserHardDeleteJdbcRepository hardDeleteJdbcRepository;
    private final WalkInProgressRedisStore walkInProgressRedisStore;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final S3Utils s3Utils;
    private final EntityManager entityManager;

    @Transactional
    public void hardDelete(UUID uid) {
        UUID actorUid = securityUtils.currentUid();
        if (actorUid.equals(uid)) {
            throw GeneralException.of(UserErrorCode.USER_HARD_DELETE_SELF);
        }
        User target = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(UserErrorCode.USER_NOT_FOUND));
        if (target.getRole() == RoleType.ADMIN) {
            throw GeneralException.of(UserErrorCode.USER_HARD_DELETE_ADMIN);
        }

        List<String> s3Keys = new ArrayList<>();
        if (target.getProfileS3Key() != null && !target.getProfileS3Key().isBlank()) {
            s3Keys.add(target.getProfileS3Key());
        }
        List<Pet> pets = petRepository.findByUser(target);
        List<Long> petIds = new ArrayList<>();
        for (Pet pet : pets) {
            petIds.add(pet.getPetId());
            if (pet.getProfileS3Key() != null && !pet.getProfileS3Key().isBlank()) {
                s3Keys.add(pet.getProfileS3Key());
            }
        }

        entityManager.flush();
        entityManager.clear();

        hardDeleteJdbcRepository.deleteAllByUid(uid);
        afterCommit(() -> {
            petIds.forEach(walkInProgressRedisStore::delete);
            refreshTokenRedisStore.deleteAll(uid.toString());
        });
        s3Keys.forEach(s3Utils::scheduleDeleteAfterCommit);
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}

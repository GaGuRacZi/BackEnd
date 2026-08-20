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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserHardDeleteServiceTest {

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private AdminUserHardDeleteJdbcRepository hardDeleteJdbcRepository;
    @Mock
    private WalkInProgressRedisStore walkInProgressRedisStore;
    @Mock
    private RefreshTokenRedisStore refreshTokenRedisStore;
    @Mock
    private S3Utils s3Utils;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AdminUserHardDeleteService adminUserHardDeleteService;

    @Test
    void 본인_계정은_하드탈퇴할_수_없다() {
        UUID uid = UUID.randomUUID();
        when(securityUtils.currentUid()).thenReturn(uid);

        assertThatThrownBy(() -> adminUserHardDeleteService.hardDelete(uid))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(UserErrorCode.USER_HARD_DELETE_SELF);
        verify(hardDeleteJdbcRepository, never()).deleteAllByUid(uid);
    }

    @Test
    void ADMIN_계정은_하드탈퇴할_수_없다() {
        UUID actor = UUID.randomUUID();
        UUID targetUid = UUID.randomUUID();
        User target = User.builder().uid(targetUid).role(RoleType.ADMIN).build();
        when(securityUtils.currentUid()).thenReturn(actor);
        when(userRepository.findById(targetUid)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> adminUserHardDeleteService.hardDelete(targetUid))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(UserErrorCode.USER_HARD_DELETE_ADMIN);
        verify(hardDeleteJdbcRepository, never()).deleteAllByUid(targetUid);
    }

    @Test
    void 대상_유저_행을_삭제한다() {
        UUID actor = UUID.randomUUID();
        UUID targetUid = UUID.randomUUID();
        User target = User.builder()
                .uid(targetUid)
                .role(RoleType.USER)
                .profileS3Key("user/a.png")
                .build();
        Pet pet = Pet.builder()
                .petId(11L)
                .user(target)
                .petName("아리")
                .birth(LocalDate.of(2020, 1, 1))
                .petWeight(new BigDecimal("4.0"))
                .profileS3Key("pet/b.png")
                .build();
        when(securityUtils.currentUid()).thenReturn(actor);
        when(userRepository.findById(targetUid)).thenReturn(Optional.of(target));
        when(petRepository.findByUser(target)).thenReturn(List.of(pet));

        adminUserHardDeleteService.hardDelete(targetUid);

        verify(walkInProgressRedisStore).delete(11L);
        verify(hardDeleteJdbcRepository).deleteAllByUid(targetUid);
        verify(refreshTokenRedisStore).deleteAll(targetUid.toString());
        verify(s3Utils).scheduleDeleteAfterCommit("user/a.png");
        verify(s3Utils).scheduleDeleteAfterCommit("pet/b.png");
    }

    @Test
    void jdbc_삭제가_실패하면_Redis는_호출되지_않는다() {
        UUID actor = UUID.randomUUID();
        UUID targetUid = UUID.randomUUID();
        User target = User.builder()
                .uid(targetUid)
                .role(RoleType.USER)
                .build();
        Pet pet = Pet.builder()
                .petId(11L)
                .user(target)
                .petName("아리")
                .birth(LocalDate.of(2020, 1, 1))
                .petWeight(new BigDecimal("4.0"))
                .build();
        when(securityUtils.currentUid()).thenReturn(actor);
        when(userRepository.findById(targetUid)).thenReturn(Optional.of(target));
        when(petRepository.findByUser(target)).thenReturn(List.of(pet));
        doThrow(new RuntimeException("jdbc fail")).when(hardDeleteJdbcRepository).deleteAllByUid(targetUid);

        assertThatThrownBy(() -> adminUserHardDeleteService.hardDelete(targetUid))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("jdbc fail");

        verify(walkInProgressRedisStore, never()).delete(any());
        verify(refreshTokenRedisStore, never()).deleteAll(any());
    }

    @Test
    void 트랜잭션이_커밋되지_않으면_Redis는_호출되지_않는다() {
        UUID actor = UUID.randomUUID();
        UUID targetUid = UUID.randomUUID();
        User target = User.builder()
                .uid(targetUid)
                .role(RoleType.USER)
                .build();
        Pet pet = Pet.builder()
                .petId(11L)
                .user(target)
                .petName("아리")
                .birth(LocalDate.of(2020, 1, 1))
                .petWeight(new BigDecimal("4.0"))
                .build();
        when(securityUtils.currentUid()).thenReturn(actor);
        when(userRepository.findById(targetUid)).thenReturn(Optional.of(target));
        when(petRepository.findByUser(target)).thenReturn(List.of(pet));

        TransactionSynchronizationManager.initSynchronization();
        try {
            adminUserHardDeleteService.hardDelete(targetUid);

            verify(hardDeleteJdbcRepository).deleteAllByUid(targetUid);
            verify(walkInProgressRedisStore, never()).delete(any());
            verify(refreshTokenRedisStore, never()).deleteAll(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}

package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.redis.RefreshTokenRedisStore;
import com.gaguraczi.paw.global.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private RefreshTokenRedisStore refreshTokenRedisStore;

    @InjectMocks
    private WithdrawalService withdrawalService;

    @Test
    void 이미_탈퇴한_계정은_다시_탈퇴할_수_없다() {
        User user = User.builder().uid(UUID.randomUUID()).build();
        user.withdraw();
        when(securityUtils.currentUser()).thenReturn(user);

        assertThatThrownBy(() -> withdrawalService.withdraw())
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getCode())
                .isEqualTo(MypageErrorCode.ALREADY_WITHDRAWN);
    }

    @Test
    void 탈퇴하면_isDeleted가_true가_되고_리프레시토큰이_삭제된다() {
        User user = User.builder().uid(UUID.randomUUID()).build();
        when(securityUtils.currentUser()).thenReturn(user);

        withdrawalService.withdraw();

        assertThat(user.isDeleted()).isTrue();
    }
}

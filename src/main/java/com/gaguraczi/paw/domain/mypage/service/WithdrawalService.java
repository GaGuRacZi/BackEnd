package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.mypage.dto.res.WithdrawalPreviewRes;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.global.redis.RefreshTokenRedisStore;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WithdrawalService {

    private final SecurityUtils securityUtils;
    private final CommunityRepository communityRepository;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final S3Utils s3Utils;
    private final Clock clock;

    public WithdrawalPreviewRes preview() {
        User user = securityUtils.currentUser();
        boolean hasOngoingMarketTrade = communityRepository.existsByUser_UidAndPostTypeAndMarketStatusIn(
                user.getUid(),
                PostType.MARKET,
                EnumSet.of(MarketStatus.IN_PROGRESS, MarketStatus.RESERVED)
        );
        return new WithdrawalPreviewRes(
                user.getSubscribe() != SubscribeType.BASIC,
                user.getSubscribe(),
                hasOngoingMarketTrade
        );
    }

    @Transactional
    public void withdraw() {
        User user = securityUtils.currentUser();
        String profileS3Key = user.getProfileS3Key();
        user.withdraw(LocalDateTime.now(clock));
        refreshTokenRedisStore.deleteAll(user.getUid().toString());
        s3Utils.scheduleDeleteAfterCommit(profileS3Key);
    }
}

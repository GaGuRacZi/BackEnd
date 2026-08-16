package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.auth.entity.OAuth;
import com.gaguraczi.paw.domain.auth.enums.SocialType;
import com.gaguraczi.paw.domain.users.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MypageProfileRes(
        UUID uid,
        String name,
        String nickname,
        String intro,
        String email,
        String profileUrl,
        String regionCode,
        String regionName,
        boolean isNew,
        List<LinkedAccount> linkedAccounts
) {
    public static MypageProfileRes of(User user, List<OAuth> linkedOAuths) {
        return new MypageProfileRes(
                user.getUid(),
                user.getName(),
                user.getNickname(),
                user.getIntro(),
                user.getEmail(),
                user.getProfileUrl(),
                user.getRegion() != null ? user.getRegion().getCode() : null,
                user.getRegion() != null ? user.getRegion().getName() : null,
                user.isNew(),
                linkedOAuths.stream().map(LinkedAccount::from).toList()
        );
    }

    public record LinkedAccount(SocialType socialType, LocalDateTime linkedAt) {
        public static LinkedAccount from(OAuth oAuth) {
            return new LinkedAccount(oAuth.getProviderType(), oAuth.getCreatedAt());
        }
    }
}

package com.gaguraczi.paw.domain.users.dto.res;

import com.gaguraczi.paw.domain.users.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserProfileRes {

    private final UUID uid;
    private final String name;
    private final String nickname;
    private final String intro;
    private final String email;
    private final String profileUrl;
    private final String regionCode;
    private final String regionName;
    private final boolean isNew;

    public static UserProfileRes from(User user) {
        return UserProfileRes.builder()
                .uid(user.getUid())
                .name(user.getName())
                .nickname(user.getNickname())
                .intro(user.getIntro())
                .email(user.getEmail())
                .profileUrl(user.getProfileUrl())
                .regionCode(user.getRegion() != null ? user.getRegion().getCode() : null)
                .regionName(user.getRegion() != null ? user.getRegion().getName() : null)
                .isNew(user.isNew())
                .build();
    }
}

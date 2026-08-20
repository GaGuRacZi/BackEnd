package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;

public record MypageHomeRes(
        String name,
        String nickname,
        String profileUrl,
        String regionName,
        MainPet mainPet,
        Subscribe subscribe,
        long unreadNotificationCount
) {
    public static MypageHomeRes of(User user, Pet mainPet, String regionName, long unreadNotificationCount) {
        SubscribeType plan = user.getSubscribe() == null ? SubscribeType.BASIC : user.getSubscribe();
        return new MypageHomeRes(
                user.getName(),
                user.getNickname(),
                user.getProfileUrl(),
                regionName,
                mainPet == null ? null : new MainPet(mainPet.getPetId(), mainPet.getPetName(), mainPet.getProfileUrl()),
                new Subscribe(plan, plan.displayName(), true),
                unreadNotificationCount
        );
    }

    public record MainPet(Long petId, String petName, String profileUrl) {
    }

    public record Subscribe(SubscribeType plan, String displayName, boolean active) {
    }
}

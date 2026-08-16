package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;

public record MypageHomeRes(
        String nickname,
        String regionName,
        MainPet mainPet,
        Subscribe subscribe
) {
    public static MypageHomeRes of(User user, Pet mainPet) {
        return new MypageHomeRes(
                user.getNickname(),
                user.getRegion() != null ? user.getRegion().getName() : null,
                mainPet == null ? null : new MainPet(mainPet.getPetId(), mainPet.getPetName(), mainPet.getProfileUrl()),
                new Subscribe(user.getSubscribe(), user.getSubscribe() != SubscribeType.BASIC)
        );
    }

    public record MainPet(Long petId, String petName, String profileUrl) {
    }

    public record Subscribe(SubscribeType planName, boolean active) {
    }
}

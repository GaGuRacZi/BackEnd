package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 홈 요약")
public record MypageHomeRes(
        @Schema(description = "보호자 이름", example = "홍길동")
        String name,
        @Schema(description = "닉네임", example = "길동이")
        String nickname,
        @Schema(description = "프로필 이미지 URL. 없으면 null", example = "https://cdn.example.com/profiles/uid.jpg")
        String profileUrl,
        @Schema(description = "표시용 지역명. 가능하면 '시/도 시군구'", example = "서울특별시 강남구")
        String regionName,
        @Schema(description = "대표 반려동물. 없으면 null")
        MainPet mainPet,
        @Schema(description = "구독 요약")
        Subscribe subscribe,
        @Schema(description = "미읽음 알림 수 (GET /notifications/unread-count와 동일 기준)", example = "3")
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

    @Schema(name = "MypageHomeMainPet", description = "대표 반려동물 요약")
    public record MainPet(
            @Schema(description = "펫 ID", example = "1")
            Long petId,
            @Schema(description = "이름", example = "초코")
            String petName,
            @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/pets/1.jpg")
            String profileUrl
    ) {
    }

    @Schema(name = "MypageHomeSubscribe", description = "구독 표시 정보")
    public record Subscribe(
            @Schema(description = "플랜 코드", example = "BASIC")
            SubscribeType plan,
            @Schema(description = "화면 표시명", example = "꼬마 젤리")
            String displayName,
            @Schema(description = "표시 활성 여부. 현재 항상 true", example = "true")
            boolean active
    ) {
    }
}

package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.auth.entity.OAuth;
import com.gaguraczi.paw.domain.auth.enums.SocialType;
import com.gaguraczi.paw.domain.users.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "마이페이지 프로필 상세. 연동 계정 목록 포함. 코인 필드는 GET /users/me를 사용하세요.")
public record MypageProfileRes(
        @Schema(description = "유저 UID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "보호자 이름", example = "홍길동")
        String name,
        @Schema(description = "닉네임", example = "길동이")
        String nickname,
        @Schema(description = "한줄소개", example = "강아지와 산책하는 걸 좋아해요")
        String intro,
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        @Schema(description = "프로필 이미지 URL. 없으면 null", example = "https://cdn.example.com/profiles/uid.jpg")
        String profileUrl,
        @Schema(description = "법정동 지역 코드. 없으면 null", example = "11680")
        String regionCode,
        @Schema(description = "지역 이름. 없으면 null", example = "강남구")
        String regionName,
        @Schema(description = "온보딩 미완료 여부", example = "false")
        boolean isNew,
        @Schema(description = "연동된 로그인 수단")
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

    @Schema(name = "MypageLinkedAccount", description = "연동된 소셜/로컬 계정")
    public record LinkedAccount(
            @Schema(description = "연동 수단", example = "KAKAO")
            SocialType socialType,
            @Schema(description = "연동 시각", example = "2026-03-01T12:00:00")
            LocalDateTime linkedAt
    ) {
        public static LinkedAccount from(OAuth oAuth) {
            return new LinkedAccount(oAuth.getProviderType(), oAuth.getCreatedAt());
        }
    }
}

package com.gaguraczi.paw.domain.users.dto.res;

import com.gaguraczi.paw.domain.users.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(description = "내 프로필. coin/usedCoin은 AI 진료 상세 요약(POST /visits/{visitId}/ai-summary)에서 사용합니다.")
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
    @Schema(description = "남은 코인. 신규 회원 기본 10. AI 상세 요약 1회에 1개 차감.", example = "9")
    private final int coin;
    @Schema(description = "사용한 코인 누적. 요약 실패 시 환불되면 줄어듭니다.", example = "1")
    private final int usedCoin;

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
                .coin(user.coinBalance())
                .usedCoin(user.usedCoinBalance())
                .build();
    }
}

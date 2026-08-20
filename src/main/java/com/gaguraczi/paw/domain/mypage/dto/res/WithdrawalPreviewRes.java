package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "탈퇴 전 확인 항목. 서버는 탈퇴를 막지 않으며, 앱에서 안내 문구를 띄울 때 사용합니다.")
public record WithdrawalPreviewRes(
        @Schema(description = "유료 구독 이용 중 여부 (BASIC이 아니면 true)", example = "false")
        boolean subscribing,
        @Schema(description = "현재 구독 플랜", example = "BASIC")
        SubscribeType subscribePlan,
        @Schema(description = "진행중(거래중/예약중) 장터 게시글 보유 여부", example = "true")
        boolean hasOngoingMarketTrade
) {
}

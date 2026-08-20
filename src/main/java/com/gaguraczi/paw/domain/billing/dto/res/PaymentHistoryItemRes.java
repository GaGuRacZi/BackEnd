package com.gaguraczi.paw.domain.billing.dto.res;

import com.gaguraczi.paw.domain.billing.entity.PaymentHistory;
import com.gaguraczi.paw.domain.billing.enums.PaymentStatus;
import com.gaguraczi.paw.domain.billing.enums.PaymentType;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "결제 내역")
public record PaymentHistoryItemRes(
        @Schema(description = "결제 ID", example = "1")
        Long paymentId,
        @Schema(description = "결제한 플랜", example = "PRO")
        SubscribeType plan,
        @Schema(description = "플랜 표시명", example = "새싹 젤리")
        String displayName,
        @Schema(description = "결제 금액(원)", example = "4900")
        int amount,
        @Schema(description = "결제 유형", example = "PURCHASE")
        PaymentType type,
        @Schema(description = "결제 상태", example = "SUCCESS")
        PaymentStatus status,
        @Schema(description = "결제일시", example = "2026-08-20T11:00:00")
        LocalDateTime paidAt
) {
    public static PaymentHistoryItemRes from(PaymentHistory payment) {
        return new PaymentHistoryItemRes(
                payment.getPaymentId(),
                payment.getPlan(),
                payment.getPlan().displayName(),
                payment.getAmount(),
                payment.getType(),
                payment.getStatus(),
                payment.getPaidAt()
        );
    }
}

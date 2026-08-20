package com.gaguraczi.paw.domain.billing.controller;

import com.gaguraczi.paw.domain.billing.dto.res.PaymentHistoryItemRes;
import com.gaguraczi.paw.domain.billing.exception.code.BillingSuccessCode;
import com.gaguraczi.paw.domain.billing.service.SubscriptionService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.api.CursorPageRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "billing", description = BillingApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/mypage/payments")
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "결제 내역 목록", description = BillingApiDocs.PAYMENT_LIST_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "BILLING_PAYMENT_LIST_200"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 커서 (MYPAGE_400)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MYPAGE_400",
                                    value = """
                                            {"isSuccess":false,"code":"MYPAGE_400","message":"요청 처리에 실패했습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "JWT_401_1", value = BillingApiDocs.JWT_401_1_EXAMPLE)
                    )
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<PaymentHistoryItemRes>> getPayments(
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(BillingSuccessCode.PAYMENT_LIST_200, subscriptionService.getPayments(cursor, size));
    }

    @Operation(summary = "결제 내역 상세", description = "본인 결제 건만 조회할 수 있습니다. 없거나 타인이면 BILLING_404_2.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "BILLING_PAYMENT_DETAIL_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = BillingApiDocs.PAYMENT_ITEM_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "JWT_401_1", value = BillingApiDocs.JWT_401_1_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "BILLING_404_2",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "BILLING_404_2",
                                    value = """
                                            {"isSuccess":false,"code":"BILLING_404_2","message":"결제 내역을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentHistoryItemRes> getPayment(
            @Parameter(description = "결제 ID", example = "1", required = true) @PathVariable Long paymentId
    ) {
        return ApiResponse.onSuccess(BillingSuccessCode.PAYMENT_DETAIL_200, subscriptionService.getPayment(paymentId));
    }
}

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
import org.springframework.http.MediaType;
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
                    description = "성공 (BILLING_PAYMENT_LIST_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "결제 목록", value = BillingApiDocs.PAYMENT_LIST_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 커서 (MYPAGE_400)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "MYPAGE_400", value = BillingApiDocs.MYPAGE_400_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "JWT_401_1", value = BillingApiDocs.JWT_401_1_EXAMPLE)
                    )
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<PaymentHistoryItemRes>> getPayments(
            @Parameter(
                    description = "이전 응답의 nextCursor. opaque 값이며 해석하지 마세요.",
                    example = "MjAyNi0wNy0yMFQyMzoxMDowMHwx"
            ) @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(BillingSuccessCode.PAYMENT_LIST_200, subscriptionService.getPayments(cursor, size));
    }

    @Operation(summary = "결제 내역 상세", description = BillingApiDocs.PAYMENT_DETAIL_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (BILLING_PAYMENT_DETAIL_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "결제 상세", value = BillingApiDocs.PAYMENT_ITEM_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "JWT_401_1", value = BillingApiDocs.JWT_401_1_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "없거나 타인 결제 (BILLING_404_2)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "BILLING_404_2", value = BillingApiDocs.BILLING_404_2_EXAMPLE)
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

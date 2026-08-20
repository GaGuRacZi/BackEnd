package com.gaguraczi.paw.domain.billing.controller;

import com.gaguraczi.paw.domain.billing.dto.req.PlanChangeReq;
import com.gaguraczi.paw.domain.billing.dto.res.SubscriptionRes;
import com.gaguraczi.paw.domain.billing.exception.code.BillingSuccessCode;
import com.gaguraczi.paw.domain.billing.service.SubscriptionService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "billing", description = BillingApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/mypage/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "현재 요금제 조회", description = BillingApiDocs.GET_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (BILLING_PLAN_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "PRO 이용 중",
                                            summary = "예약 없음",
                                            value = BillingApiDocs.SUBSCRIPTION_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "다운그레이드 예약",
                                            summary = "periodEnd까지 PRO 유지, pendingPlan=BASIC",
                                            value = BillingApiDocs.SUBSCRIPTION_PENDING_EXAMPLE
                                    )
                            }
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
    public ApiResponse<SubscriptionRes> getCurrent() {
        return ApiResponse.onSuccess(BillingSuccessCode.PLAN_GET_200, subscriptionService.getCurrent());
    }

    @Operation(
            summary = "요금제 변경/예약",
            description = BillingApiDocs.CHANGE_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlanChangeReq.class),
                            examples = {
                                    @ExampleObject(
                                            name = "업그레이드",
                                            summary = "PRO로 즉시 변경",
                                            value = BillingApiDocs.CHANGE_REQ_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "다운그레이드 예약",
                                            summary = "BASIC으로 해지 예약",
                                            value = BillingApiDocs.DOWNGRADE_REQ_EXAMPLE
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (BILLING_PLAN_CHANGE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "즉시 업그레이드",
                                            summary = "ULTIMATE 적용 + 결제 내역 생성",
                                            value = BillingApiDocs.SUBSCRIPTION_CHANGE_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "다운그레이드 예약",
                                            summary = "현재 PRO 유지, pendingPlan=BASIC",
                                            value = BillingApiDocs.SUBSCRIPTION_DOWNGRADE_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "BILLING_400_1 이미 이용 중 / COMMON_400 유효성",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "BILLING_400_1", value = BillingApiDocs.BILLING_400_1_EXAMPLE),
                                    @ExampleObject(name = "COMMON_400", value = BillingApiDocs.COMMON_400_PLAN_EXAMPLE)
                            }
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
    @PostMapping
    public ApiResponse<SubscriptionRes> changePlan(@Valid @RequestBody PlanChangeReq req) {
        return ApiResponse.onSuccess(BillingSuccessCode.PLAN_CHANGE_200, subscriptionService.changePlan(req));
    }
}

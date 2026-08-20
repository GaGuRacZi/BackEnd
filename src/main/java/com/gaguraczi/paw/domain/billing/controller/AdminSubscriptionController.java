package com.gaguraczi.paw.domain.billing.controller;

import com.gaguraczi.paw.domain.billing.dto.req.AdminSubscriptionForceReq;
import com.gaguraczi.paw.domain.billing.dto.res.SubscriptionRes;
import com.gaguraczi.paw.domain.billing.exception.code.BillingSuccessCode;
import com.gaguraczi.paw.domain.billing.service.SubscriptionService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "admin-billing", description = "관리자 요금제 API. ADMIN 역할 필요.")
@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "요금제 강제 변경",
            description = "ADMIN JWT 필수. 대상 uid의 요금제를 즉시 적용합니다. 다음 결제일 대기를 건너뛰며 결제 내역은 남기지 않습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "BILLING_PLAN_FORCE_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "성공", value = BillingApiDocs.SUBSCRIPTION_EXAMPLE)
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
                    responseCode = "403",
                    description = BillingApiDocs.JWT_403_3_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "JWT_403_3", value = BillingApiDocs.JWT_403_3_EXAMPLE)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "BILLING_404_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "BILLING_404_1",
                                    value = """
                                            {"isSuccess":false,"code":"BILLING_404_1","message":"사용자를 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/force")
    public ApiResponse<SubscriptionRes> force(@Valid @RequestBody AdminSubscriptionForceReq req) {
        return ApiResponse.onSuccess(
                BillingSuccessCode.PLAN_FORCE_200,
                subscriptionService.forceChange(req.uid(), req.plan())
        );
    }
}

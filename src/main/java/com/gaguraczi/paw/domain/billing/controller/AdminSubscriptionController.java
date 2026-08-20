package com.gaguraczi.paw.domain.billing.controller;

import com.gaguraczi.paw.domain.billing.dto.req.AdminSubscriptionForceReq;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "admin-billing", description = BillingApiDocs.ADMIN_BILLING_TAG_DESCRIPTION)
@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "요금제 강제 변경",
            description = BillingApiDocs.FORCE_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminSubscriptionForceReq.class),
                            examples = @ExampleObject(
                                    name = "BASIC으로 강제 해지",
                                    value = BillingApiDocs.FORCE_REQ_EXAMPLE
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (BILLING_PLAN_FORCE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "BASIC 즉시 적용",
                                    value = BillingApiDocs.SUBSCRIPTION_FORCE_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류 (COMMON_400)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "COMMON_400", value = BillingApiDocs.COMMON_400_PLAN_EXAMPLE)
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
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "JWT_403_2",
                                            summary = "유효하지 않은 token",
                                            value = BillingApiDocs.JWT_403_2_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "JWT_403_3",
                                            summary = "ADMIN 권한 없음",
                                            value = BillingApiDocs.JWT_403_3_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (BILLING_404_1)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "BILLING_404_1", value = BillingApiDocs.BILLING_404_1_EXAMPLE)
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

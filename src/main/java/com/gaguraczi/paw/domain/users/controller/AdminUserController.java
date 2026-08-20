package com.gaguraczi.paw.domain.users.controller;

import com.gaguraczi.paw.domain.billing.controller.BillingApiDocs;
import com.gaguraczi.paw.domain.users.exception.code.UserSuccessCode;
import com.gaguraczi.paw.domain.users.service.AdminUserHardDeleteService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "admin-users", description = "관리자 계정 API. ADMIN 역할 필요.")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserHardDeleteService adminUserHardDeleteService;

    @Operation(
            summary = "계정 하드탈퇴",
            description = """
                    ADMIN JWT 필수. 대상 유저와 연관 DB 행을 물리 삭제합니다.
                    본인·ADMIN 계정은 삭제할 수 없습니다. 소프트 탈퇴된 계정도 대상입니다.
                    해당 유저의 커뮤니티 글/댓글/채팅방도 삭제됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "USER_HARD_DELETE_200",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {"isSuccess":true,"code":"USER_HARD_DELETE_200","message":"계정이 삭제되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "본인 또는 ADMIN 계정 (USER_400_4 / USER_400_5)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = BillingApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(examples = @ExampleObject(value = BillingApiDocs.JWT_401_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = BillingApiDocs.JWT_403_3_DESCRIPTION,
                    content = @Content(examples = @ExampleObject(value = BillingApiDocs.JWT_403_3_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "USER_404",
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {"isSuccess":false,"code":"USER_404","message":"사용자를 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{uid}")
    public ApiResponse<Void> hardDelete(
            @Parameter(description = "대상 유저 uid", required = true) @PathVariable UUID uid
    ) {
        adminUserHardDeleteService.hardDelete(uid);
        return ApiResponse.onSuccess(UserSuccessCode.USER_HARD_DELETE_200, null);
    }
}

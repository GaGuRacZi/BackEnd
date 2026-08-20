package com.gaguraczi.paw.domain.users.controller;

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
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "admin-users", description = AdminUserApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserHardDeleteService adminUserHardDeleteService;

    @Operation(summary = "계정 하드탈퇴", description = AdminUserApiDocs.HARD_DELETE_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (USER_HARD_DELETE_200)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "삭제 완료",
                                    value = AdminUserApiDocs.HARD_DELETE_200_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "본인 또는 ADMIN 계정 (USER_400_4 / USER_400_5)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "USER_400_4",
                                            summary = "본인 계정",
                                            value = AdminUserApiDocs.USER_400_4_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "USER_400_5",
                                            summary = "관리자 계정",
                                            value = AdminUserApiDocs.USER_400_5_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = AdminUserApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "JWT_401_1", value = AdminUserApiDocs.JWT_401_1_EXAMPLE)
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
                                            value = AdminUserApiDocs.JWT_403_2_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "JWT_403_3",
                                            summary = "ADMIN 권한 없음",
                                            value = AdminUserApiDocs.JWT_403_3_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음 (USER_404)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "USER_404", value = AdminUserApiDocs.USER_404_EXAMPLE)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{uid}")
    public ApiResponse<Void> hardDelete(
            @Parameter(
                    description = "대상 유저 uid",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            ) @PathVariable UUID uid
    ) {
        adminUserHardDeleteService.hardDelete(uid);
        return ApiResponse.onSuccess(UserSuccessCode.USER_HARD_DELETE_200, null);
    }
}

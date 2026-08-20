package com.gaguraczi.paw.domain.notification.controller;

import com.gaguraczi.paw.domain.notification.dto.res.NotificationItemRes;
import com.gaguraczi.paw.domain.notification.dto.res.NotificationReadAllRes;
import com.gaguraczi.paw.domain.notification.dto.res.NotificationUnreadCountRes;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.exception.code.NotificationSuccessCode;
import com.gaguraczi.paw.domain.notification.service.NotificationInboxQueryService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "notifications", description = NotificationApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationInboxQueryService notificationInboxQueryService;

    @Operation(summary = "알림 목록", description = NotificationApiDocs.LIST_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "NOTI_LIST_200.",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "전체", value = NotificationApiDocs.LIST_200_EXAMPLE),
                            @ExampleObject(name = "CHAT만", value = NotificationApiDocs.LIST_CHAT_200_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "NOTI_400. 커서 변조/파싱 실패.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "NOTI_400", value = NotificationApiDocs.NOTI_400_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = NotificationApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = NotificationApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<NotificationItemRes>> list(
            @Parameter(description = "TODO | AI | COMMUNITY | CHAT | EMERGENCY. 생략 시 전체", example = "CHAT")
            @RequestParam(required = false) NotificationCategory category,
            @Parameter(description = "이전 응답 nextCursor", example = "MjAyNi0wOC0yMFQwOTowMDowMHwxMDE")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_LIST_200,
                notificationInboxQueryService.list(category, cursor, size)
        );
    }

    @Operation(summary = "미읽음 알림 수", description = NotificationApiDocs.UNREAD_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "NOTI_UNREAD_200.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "NOTI_UNREAD_200", value = NotificationApiDocs.UNREAD_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = NotificationApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = NotificationApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountRes> unreadCount() {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_UNREAD_COUNT_200,
                notificationInboxQueryService.unreadCount()
        );
    }

    @Operation(summary = "알림 읽음", description = NotificationApiDocs.READ_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "NOTI_READ_200.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "NOTI_READ_200", value = NotificationApiDocs.READ_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = NotificationApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = NotificationApiDocs.JWT_401_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "NOTI_404. 없거나 타인 알림.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "NOTI_404", value = NotificationApiDocs.NOTI_404_EXAMPLE))
            )
    })
    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationItemRes> markRead(
            @Parameter(description = "알림 ID", example = "98", required = true)
            @PathVariable Long id
    ) {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_READ_200,
                notificationInboxQueryService.markRead(id)
        );
    }

    @Operation(summary = "모두 읽음", description = NotificationApiDocs.READ_ALL_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "NOTI_READ_ALL_200.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "NOTI_READ_ALL_200", value = NotificationApiDocs.READ_ALL_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = NotificationApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = NotificationApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @PatchMapping("/read-all")
    public ApiResponse<NotificationReadAllRes> markAllRead() {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_READ_ALL_200,
                notificationInboxQueryService.markAllRead()
        );
    }
}

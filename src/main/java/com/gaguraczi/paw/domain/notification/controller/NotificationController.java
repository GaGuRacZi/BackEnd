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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "notifications", description = "알림 인박스 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationInboxQueryService notificationInboxQueryService;

    @Operation(
            summary = "알림 목록",
            description = "Access Token(JWT) 필수. category로 전체/할 일/AI/커뮤니티/긴급 필터. 오늘·어제 그룹은 앱이 createdAt(KST)으로 나눕니다."
    )
    @GetMapping
    public ApiResponse<CursorPageRes<NotificationItemRes>> list(
            @Parameter(description = "TODO | AI | COMMUNITY | EMERGENCY. 생략 시 전체")
            @RequestParam(required = false) NotificationCategory category,
            @Parameter(description = "이전 응답의 nextCursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기, 기본 20 최대 50") @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_LIST_200,
                notificationInboxQueryService.list(category, cursor, size)
        );
    }

    @Operation(summary = "미읽음 알림 수")
    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountRes> unreadCount() {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_UNREAD_COUNT_200,
                notificationInboxQueryService.unreadCount()
        );
    }

    @Operation(summary = "알림 읽음")
    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationItemRes> markRead(@PathVariable Long id) {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_READ_200,
                notificationInboxQueryService.markRead(id)
        );
    }

    @Operation(summary = "모두 읽음")
    @PatchMapping("/read-all")
    public ApiResponse<NotificationReadAllRes> markAllRead() {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_READ_ALL_200,
                notificationInboxQueryService.markAllRead()
        );
    }
}

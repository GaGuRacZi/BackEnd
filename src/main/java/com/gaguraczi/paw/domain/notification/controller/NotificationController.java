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

@Tag(
        name = "notifications",
        description = "알림 인박스 API. JWT Bearer 필수. 오늘/어제 그룹은 앱이 createdAt(KST)으로 나눕니다. "
                + "채널 on/off·방해 금지는 PATCH /mypage/notifications/settings, FCM 토큰은 PUT /users/me/push-token."
)
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationInboxQueryService notificationInboxQueryService;

    @Operation(
            summary = "알림 목록",
            description = """
                    Access Token(JWT) 필수. 최신순 커서 페이지네이션.
                    - category: TODO | AI | COMMUNITY | EMERGENCY. 생략 시 전체
                    - size 기본 20, 최대 50
                    - cursor는 이전 응답 nextCursor를 그대로 전달 (변조 금지)
                    - `ctaLabel` + `targetType` + `targetId`로 화면 이동
                      (TODO=할 일, VISIT=진료, POST=게시글, MAP=지도)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (NOTI_LIST_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "알림 목록",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "NOTI_LIST_200",
                                              "message": "알림 목록 조회에 성공했습니다.",
                                              "result": {
                                                "content": [
                                                  {
                                                    "id": 101,
                                                    "category": "TODO",
                                                    "title": "슬개골 영양제 체크가 필요해요",
                                                    "body": "오늘 09:00 · 미완료 상태예요",
                                                    "isRead": false,
                                                    "createdAt": "2026-08-20T09:00:00",
                                                    "ctaLabel": "할 일 보기",
                                                    "targetType": "TODO",
                                                    "targetId": 15
                                                  },
                                                  {
                                                    "id": 100,
                                                    "category": "AI",
                                                    "title": "AI 진료 요약이 완료됐어요",
                                                    "body": "진료 녹음 분석 결과 확인 가능",
                                                    "isRead": true,
                                                    "createdAt": "2026-08-19T18:10:00",
                                                    "ctaLabel": "요약 보기",
                                                    "targetType": "VISIT",
                                                    "targetId": 7
                                                  },
                                                  {
                                                    "id": 99,
                                                    "category": "COMMUNITY",
                                                    "title": "내 게시글에 댓글이 달렸어요",
                                                    "body": "한강공원 쪽 추천해요.",
                                                    "isRead": false,
                                                    "createdAt": "2026-08-19T12:00:00",
                                                    "ctaLabel": "글 보기",
                                                    "targetType": "POST",
                                                    "targetId": 10
                                                  }
                                                ],
                                                "nextCursor": "MjAyNi0wOC0xOVQxMjowMDowMHw5OQ",
                                                "hasNext": true,
                                                "size": 20
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 커서 (NOTI_400)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "NOTI_400",
                                    value = """
                                            {"isSuccess":false,"code":"NOTI_400","message":"알림 커서가 올바르지 않습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<CursorPageRes<NotificationItemRes>> list(
            @Parameter(description = "TODO | AI | COMMUNITY | EMERGENCY. 생략 시 전체", example = "TODO")
            @RequestParam(required = false) NotificationCategory category,
            @Parameter(description = "이전 응답의 nextCursor", example = "MjAyNi0wOC0yMFQwOTowMDowMHwxMDE")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기. 기본 20, 최대 50", example = "20")
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_LIST_200,
                notificationInboxQueryService.list(category, cursor, size)
        );
    }

    @Operation(
            summary = "미읽음 알림 수",
            description = "Access Token(JWT) 필수. `isRead=false` 개수입니다. 마이페이지 홈의 unreadNotificationCount와 동일 기준입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (NOTI_UNREAD_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "미읽음 수",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "NOTI_UNREAD_200",
                                              "message": "미읽음 알림 수 조회에 성공했습니다.",
                                              "result": { "count": 3 }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountRes> unreadCount() {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_UNREAD_COUNT_200,
                notificationInboxQueryService.unreadCount()
        );
    }

    @Operation(
            summary = "알림 읽음",
            description = """
                    Access Token(JWT) 필수. 본인 알림만 읽음 처리합니다.
                    - 이미 읽은 알림을 다시 호출해도 200 (idempotent)
                    - 없거나 타인 알림이면 NOTI_404
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (NOTI_READ_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "읽음 처리",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "NOTI_READ_200",
                                              "message": "알림을 읽음 처리했습니다.",
                                              "result": {
                                                "id": 101,
                                                "category": "TODO",
                                                "title": "슬개골 영양제 체크가 필요해요",
                                                "body": "오늘 09:00 · 미완료 상태예요",
                                                "isRead": true,
                                                "createdAt": "2026-08-20T09:00:00",
                                                "ctaLabel": "할 일 보기",
                                                "targetType": "TODO",
                                                "targetId": 15
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "알림 없음/타인 알림 (NOTI_404)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "NOTI_404",
                                    value = """
                                            {"isSuccess":false,"code":"NOTI_404","message":"알림을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationItemRes> markRead(
            @Parameter(description = "알림 ID", example = "101", required = true)
            @PathVariable Long id
    ) {
        return ApiResponse.onSuccess(
                NotificationSuccessCode.NOTIFICATION_READ_200,
                notificationInboxQueryService.markRead(id)
        );
    }

    @Operation(
            summary = "모두 읽음",
            description = """
                    Access Token(JWT) 필수. 내 미읽음 알림을 전부 읽음 처리합니다.
                    - `updatedCount`는 이번에 바뀐 행 수입니다. 이미 모두 읽었으면 0
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (NOTI_READ_ALL_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "모두 읽음",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "NOTI_READ_ALL_200",
                                              "message": "모든 알림을 읽음 처리했습니다.",
                                              "result": { "updatedCount": 5 }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증 (JWT_401_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
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

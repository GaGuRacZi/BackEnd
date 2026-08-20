package com.gaguraczi.paw.domain.mypage.controller;

import com.gaguraczi.paw.domain.mypage.dto.req.NotificationSettingUpdateReq;
import com.gaguraczi.paw.domain.mypage.dto.res.NotificationSettingRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageSuccessCode;
import com.gaguraczi.paw.domain.mypage.service.NotificationSettingService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "mypage",
        description = "마이페이지 API. 공지사항 목록/상세만 인증 불필요. 그 외 JWT Bearer 필수. "
                + "커서(nextCursor)는 opaque 값으로 다음 요청에 그대로 전달하세요."
)
@RestController
@RequestMapping("/mypage/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @Operation(
            summary = "알림 설정 조회",
            description = """
                    Access Token(JWT) 필수.
                    - 설정이 없으면 기본값으로 생성 후 반환합니다.
                    - 기본값: 할 일/건강 이상/AI/커뮤니티 ON, **채팅/혜택 OFF**, 방해 금지 22:00~07:00 ON
                    - `items`는 Figma 카피·순서입니다. PATCH에는 boolean 필드명(`todoAlarm` 등)을 쓰세요.
                    - 건강 이상 알림은 방해 금지 시간에도 FCM이 나갈 수 있습니다. 채팅은 DND 예외가 아닙니다.
                    - 채팅 푸시/인박스는 `chatAlarm=true` 이고 상대가 메시지를 보낸 뒤에만 쌓입니다. 토큰은 `PUT /users/me/push-token`.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_NOTI_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "기본 설정",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_NOTI_200",
                                              "message": "알림 설정 조회에 성공했습니다.",
                                              "result": {
                                                "todoAlarm": true,
                                                "healthAlarm": true,
                                                "aiAnalysisAlarm": true,
                                                "communityAlarm": true,
                                                "chatAlarm": false,
                                                "benefitAlarm": false,
                                                "dndEnabled": true,
                                                "dndStart": "22:00:00",
                                                "dndEnd": "07:00:00",
                                                "items": [
                                                  {
                                                    "key": "todoAlarm",
                                                    "title": "할 일 알림",
                                                    "description": "오늘의 할 일과 복약 시간을 알려줘요",
                                                    "enabled": true
                                                  },
                                                  {
                                                    "key": "healthAlarm",
                                                    "title": "건강 이상 알림",
                                                    "description": "기록에서 주의가 필요한 변화를 알려줘요",
                                                    "enabled": true
                                                  },
                                                  {
                                                    "key": "aiAnalysisAlarm",
                                                    "title": "AI 분석 완료 알림",
                                                    "description": "진료 요약과 OCR 분석 완료를 알려줘요",
                                                    "enabled": true
                                                  },
                                                  {
                                                    "key": "communityAlarm",
                                                    "title": "커뮤니티 알림",
                                                    "description": "댓글, 답글, 거래 문의를 알려줘요",
                                                    "enabled": true
                                                  },
                                                  {
                                                    "key": "chatAlarm",
                                                    "title": "채팅 알림",
                                                    "description": "새 메시지와 거래 대화를 알려줘요",
                                                    "enabled": false
                                                  },
                                                  {
                                                    "key": "benefitAlarm",
                                                    "title": "혜택 이벤트 알림",
                                                    "description": "PAW 혜택과 이벤트 소식을 받아요",
                                                    "enabled": false
                                                  }
                                                ],
                                                "dnd": {
                                                  "enabled": true,
                                                  "start": "22:00:00",
                                                  "end": "07:00:00",
                                                  "title": "방해 금지 시간",
                                                  "description": "건강 이상 알림은 받을 수 있어요."
                                                }
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
            )
    })
    @GetMapping
    public ApiResponse<NotificationSettingRes> get() {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTIFICATION_SETTING_GET_200, notificationSettingService.get());
    }

    @Operation(
            summary = "알림 설정 수정 (개별/일괄)",
            description = """
                    Access Token(JWT) 필수. 보낸 필드만 부분 반영됩니다.
                    - 개별 토글: `{ "chatAlarm": true }` 처럼 보낸 필드만 반영합니다.
                    - 채팅 알림을 켜야 `POST /chat/rooms/{roomId}/messages` 상대에게 인박스·FCM이 갑니다.
                    - 방해 금지: `dndStart`와 `dndEnd`는 함께 보내야 합니다. 한쪽만 보내면 MYPAGE_400_1
                    - 자정 넘김(22:00~07:00)을 허용합니다. 채팅 FCM은 DND 구간에 나가지 않습니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "채팅 알림 켜기",
                                            value = """
                                                    { "chatAlarm": true }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "혜택 알림 켜기",
                                            value = """
                                                    { "benefitAlarm": true }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "방해 금지 시간",
                                            value = """
                                                    {
                                                      "dndEnabled": true,
                                                      "dndStart": "22:00",
                                                      "dndEnd": "07:00"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공 (MYPAGE_NOTI_UPDATE_200)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "수정 성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MYPAGE_NOTI_UPDATE_200",
                                              "message": "알림 설정이 수정되었습니다.",
                                              "result": {
                                                "todoAlarm": true,
                                                "healthAlarm": true,
                                                "aiAnalysisAlarm": true,
                                                "communityAlarm": true,
                                                "chatAlarm": false,
                                                "benefitAlarm": true,
                                                "dndEnabled": true,
                                                "dndStart": "22:00:00",
                                                "dndEnd": "07:00:00",
                                                "items": [
                                                  {
                                                    "key": "todoAlarm",
                                                    "title": "할 일 알림",
                                                    "description": "오늘의 할 일과 복약 시간을 알려줘요",
                                                    "enabled": true
                                                  }
                                                ],
                                                "dnd": {
                                                  "enabled": true,
                                                  "start": "22:00:00",
                                                  "end": "07:00:00",
                                                  "title": "방해 금지 시간",
                                                  "description": "건강 이상 알림은 받을 수 있어요."
                                                }
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "방해 금지 시간대 유효성 오류 (MYPAGE_400_1)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MYPAGE_400_1",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "MYPAGE_400_1",
                                              "message": "방해 금지 시간대는 시작/종료 시각을 함께 입력해야 합니다.",
                                              "result": null
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
    @PatchMapping
    public ApiResponse<NotificationSettingRes> update(@Valid @RequestBody NotificationSettingUpdateReq req) {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTIFICATION_SETTING_UPDATE_200, notificationSettingService.update(req));
    }
}

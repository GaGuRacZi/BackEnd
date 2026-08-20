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

@Tag(name = "mypage", description = "마이페이지 API")
@RestController
@RequestMapping("/mypage/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @Operation(
            summary = "알림 설정 조회",
            description = "Access Token(JWT) 필수. 설정이 없으면 기본값으로 생성 후 반환합니다."
    )
    @GetMapping
    public ApiResponse<NotificationSettingRes> get() {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTIFICATION_SETTING_GET_200, notificationSettingService.get());
    }

    @Operation(
            summary = "알림 설정 수정 (개별/일괄)",
            description = "Access Token(JWT) 필수. 보낸 필드만 부분 반영됩니다. dndStart/dndEnd는 함께 보내야 합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "방해 금지 시간대 유효성 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MYPAGE_400_1",
                                    value = "{\"isSuccess\":false,\"code\":\"MYPAGE_400_1\",\"message\":\"방해 금지 시간대는 시작/종료 시각을 함께 입력해야 합니다.\",\"result\":null}"
                            )
                    )
            )
    })
    @PatchMapping
    public ApiResponse<NotificationSettingRes> update(@Valid @RequestBody NotificationSettingUpdateReq req) {
        return ApiResponse.onSuccess(MypageSuccessCode.NOTIFICATION_SETTING_UPDATE_200, notificationSettingService.update(req));
    }
}

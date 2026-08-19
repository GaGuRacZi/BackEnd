package com.gaguraczi.paw.domain.notification.controller;

import com.gaguraczi.paw.domain.notification.dto.req.DeviceTokenDeleteReq;
import com.gaguraczi.paw.domain.notification.dto.req.DeviceTokenRegisterReq;
import com.gaguraczi.paw.domain.notification.exception.code.NotificationSuccessCode;
import com.gaguraczi.paw.domain.notification.service.DeviceTokenService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "device-tokens", description = "FCM 디바이스 토큰 등록/삭제. 한 유저가 여러 기기를 등록할 수 있음(멀티 디바이스). JWT Bearer 필수.")
@RestController
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @Operation(
            summary = "디바이스 토큰 등록/갱신",
            description = "로그인/앱 실행/토큰 재발급 시 호출. 이미 등록된 토큰이면 소유자·플랫폼을 현재 유저 기준으로 갱신한다(upsert)."
    )
    @PutMapping("/devices/tokens")
    public ApiResponse<Void> register(@RequestBody @Valid DeviceTokenRegisterReq req) {
        deviceTokenService.register(req.token(), req.platform());
        return ApiResponse.onSuccess(NotificationSuccessCode.DEVICE_TOKEN_REGISTER_200, null);
    }

    @Operation(
            summary = "디바이스 토큰 삭제",
            description = "로그아웃 시 현재 기기의 토큰만 삭제한다. 회원탈퇴 시에는 별도로 유저의 전체 토큰이 일괄 삭제된다."
    )
    @DeleteMapping("/devices/tokens")
    public ApiResponse<Void> unregister(@RequestBody @Valid DeviceTokenDeleteReq req) {
        deviceTokenService.unregister(req.token());
        return ApiResponse.onSuccess(NotificationSuccessCode.DEVICE_TOKEN_DELETE_200, null);
    }
}

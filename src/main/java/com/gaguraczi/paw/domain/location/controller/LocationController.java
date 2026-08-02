package com.gaguraczi.paw.domain.location.controller;

import com.gaguraczi.paw.domain.location.dto.res.AddressRes;
import com.gaguraczi.paw.domain.location.dto.res.CoordinateResolveRes;
import com.gaguraczi.paw.domain.location.dto.res.UserLocationRes;
import com.gaguraczi.paw.domain.location.exception.code.LocationSuccessCode;
import com.gaguraczi.paw.domain.location.service.LocationService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "location", description = "위치 인증/조회 (네이버 지도 + 법정동)")
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "내 위치 조회")
    @GetMapping("/user")
    public ApiResponse<UserLocationRes> getMyLocation() {
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_USER_GET_200, locationService.getMyLocation());
    }

    @Operation(summary = "내 위치 인증 (위·경도 → 시군구 LegalRegion 저장)")
    @PostMapping("/user/cert")
    public ApiResponse<UserLocationRes> certifyMyLocation(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_USER_CERT_200,
                locationService.certifyMyLocation(lat, lng)
        );
    }

    @Operation(summary = "좌표 → 표시용 주소 조회")
    @GetMapping("/address")
    public ApiResponse<AddressRes> getRoadAddress(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_ADDRESS_200,
                locationService.getRoadAddress(lat, lng)
        );
    }

    @Operation(
            summary = "좌표 → regionCode(시군구) + 주소",
            description = "온보딩 시 regionCode를 서버에서 확정할 때 사용합니다. 인증 불필요."
    )
    @GetMapping("/resolve")
    public ApiResponse<CoordinateResolveRes> resolve(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_RESOLVE_200,
                locationService.resolve(lat, lng)
        );
    }
}

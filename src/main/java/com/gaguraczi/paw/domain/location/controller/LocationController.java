package com.gaguraczi.paw.domain.location.controller;

import com.gaguraczi.paw.domain.location.dto.res.AddressRes;
import com.gaguraczi.paw.domain.location.dto.res.CoordinateResolveRes;
import com.gaguraczi.paw.domain.location.dto.res.UserLocationRes;
import com.gaguraczi.paw.domain.location.exception.code.LocationSuccessCode;
import com.gaguraczi.paw.domain.location.service.LocationService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Tag(name = "location", description = "위치 인증/조회 (네이버 지도 + 법정동)")
@RestController
@RequestMapping("/location")
@Validated
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationController {

    private final LocationService locationService;

    @Operation(
            summary = "내 위치 조회",
            description = "JWT 필수. 저장된 시군구 LegalRegion + 좌표를 반환합니다. 위치 미등록 시 LOCATION_404_3."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "LOCATION_200_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "LOCATION_200_1",
                                              "message": "내 위치 조회에 성공했습니다.",
                                              "result": {
                                                "regionCode": "1111000000",
                                                "regionName": "서울특별시 종로구",
                                                "address": "서울특별시 종로구",
                                                "latitude": 37.5665,
                                                "longitude": 126.9780
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "LOCATION_404_3",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "위치 미등록",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "LOCATION_404_3",
                                              "message": "등록된 위치가 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/user")
    public ApiResponse<UserLocationRes> getMyLocation() {
        return ApiResponse.onSuccess(LocationSuccessCode.LOCATION_USER_GET_200, locationService.getMyLocation());
    }

    @Operation(
            summary = "내 위치 인증 (위·경도 → 시군구 LegalRegion 저장)",
            description = "JWT 필수. 네이버 리버스 지오코딩으로 법정동 코드를 구한 뒤 시군구 LegalRegion에 매핑해 저장합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "LOCATION_200_2",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "LOCATION_200_2",
                                              "message": "위치 인증에 성공했습니다.",
                                              "result": {
                                                "regionCode": "1111000000",
                                                "regionName": "서울특별시 종로구",
                                                "address": "서울특별시 종로구 세종대로 110",
                                                "latitude": 37.5665,
                                                "longitude": 126.9780
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "LOCATION_400_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "잘못된 좌표",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "LOCATION_400_1",
                                              "message": "잘못된 위치 요청입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "LOCATION_404_1 / LOCATION_404_2",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "주소 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "LOCATION_404_1",
                                                      "message": "주소 결과를 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "법정동 코드 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "LOCATION_404_2",
                                                      "message": "법정동 코드 결과를 찾을 수 없습니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "LOCATION_502_2 / LOCATION_502_3",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "리버스 지오코딩 실패",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "LOCATION_502_2",
                                              "message": "리버스 지오코딩 API 호출에 실패했습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/user/cert")
    public ApiResponse<UserLocationRes> certifyMyLocation(
            @Parameter(description = "위도", example = "37.5665", required = true)
            @RequestParam
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
            double lat,
            @Parameter(description = "경도", example = "126.9780", required = true)
            @RequestParam
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
            double lng
    ) {
        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_USER_CERT_200,
                locationService.certifyMyLocation(lat, lng)
        );
    }

    @Operation(
            summary = "좌표 → 표시용 주소 조회",
            description = "JWT 필수. 네이버 리버스 지오코딩으로 도로명/지번 표시 주소를 조회합니다. DB에는 저장하지 않습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "LOCATION_200_3",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "LOCATION_200_3",
                                              "message": "도로명 주소 조회에 성공했습니다.",
                                              "result": {
                                                "address": "서울특별시 종로구 세종대로 110"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "LOCATION_400_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "잘못된 좌표",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "LOCATION_400_1",
                                              "message": "잘못된 위치 요청입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "LOCATION_404_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "주소 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "LOCATION_404_1",
                                              "message": "주소 결과를 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/address")
    public ApiResponse<AddressRes> getRoadAddress(
            @Parameter(description = "위도", example = "37.5665", required = true)
            @RequestParam
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
            double lat,
            @Parameter(description = "경도", example = "126.9780", required = true)
            @RequestParam
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
            double lng
    ) {
        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_ADDRESS_200,
                locationService.getRoadAddress(lat, lng)
        );
    }

    @Operation(
            summary = "좌표 → regionCode(시군구) + 주소",
            description = "JWT 필수. 온보딩 시 regionCode를 서버에서 확정할 때 사용합니다. DB 저장 없음."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "LOCATION_200_4",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "LOCATION_200_4",
                                              "message": "좌표 기준 지역/주소 조회에 성공했습니다.",
                                              "result": {
                                                "regionCode": "1111000000",
                                                "regionName": "서울특별시 종로구",
                                                "address": "서울특별시 종로구 세종대로 110",
                                                "latitude": 37.5665,
                                                "longitude": 126.9780
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "LOCATION_400_1 / REGION_400",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "잘못된 좌표",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "LOCATION_400_1",
                                                      "message": "잘못된 위치 요청입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "유효하지 않은 지역",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "REGION_400",
                                                      "message": "유효하지 않은 지역 코드입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_401_1",
                                              "message": "token 유효기간이 만료되었습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "LOCATION_404_2",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "법정동 코드 없음",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "LOCATION_404_2",
                                              "message": "법정동 코드 결과를 찾을 수 없습니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/resolve")
    public ApiResponse<CoordinateResolveRes> resolve(
            @Parameter(description = "위도", example = "37.5665", required = true)
            @RequestParam
            @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다.")
            double lat,
            @Parameter(description = "경도", example = "126.9780", required = true)
            @RequestParam
            @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다.")
            double lng
    ) {
        return ApiResponse.onSuccess(
                LocationSuccessCode.LOCATION_RESOLVE_200,
                locationService.resolve(lat, lng)
        );
    }
}

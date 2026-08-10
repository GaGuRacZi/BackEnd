package com.gaguraczi.paw.domain.region.controller;

import com.gaguraczi.paw.domain.region.dto.res.RegionSyncRes;
import com.gaguraczi.paw.domain.region.exception.code.RegionSuccessCode;
import com.gaguraczi.paw.domain.region.service.LegalRegionSyncService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "admin-regions", description = "법정동 관리 API")
@RestController
@RequestMapping("/admin/regions")
@RequiredArgsConstructor
public class AdminRegionController {

    private final LegalRegionSyncService legalRegionSyncService;

    @Operation(
            summary = "법정동 코드 파일 upsert 동기화",
            description = "ADMIN 역할 필요. classpath 법정동 코드 파일을 upsert합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "REGION_SYNC_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REGION_SYNC_200",
                                              "message": "법정동 동기화에 성공했습니다.",
                                              "result": {
                                                "processed": 20500,
                                                "totalAfter": 20500
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
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "JWT_403_2",
                                            summary = "유효하지 않은 token",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "JWT_403_2",
                                                      "message": "유효하지 않은 token입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "JWT_403_3",
                                            summary = "ADMIN 권한 없음",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "JWT_403_3",
                                                      "message": "권한 정보가 없는 token입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ApiResponse<RegionSyncRes> sync() {
        LegalRegionSyncService.SyncResult result = legalRegionSyncService.syncFromClasspath();
        return ApiResponse.onSuccess(
                RegionSuccessCode.REGION_SYNC_200,
                RegionSyncRes.from(result)
        );
    }
}

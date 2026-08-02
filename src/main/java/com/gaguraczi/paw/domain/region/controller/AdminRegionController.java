package com.gaguraczi.paw.domain.region.controller;

import com.gaguraczi.paw.domain.region.exception.code.RegionSuccessCode;
import com.gaguraczi.paw.domain.region.service.LegalRegionSyncService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "admin-regions", description = "법정동 관리 API")
@RestController
@RequestMapping("/admin/regions")
@RequiredArgsConstructor
public class AdminRegionController {

    private final LegalRegionSyncService legalRegionSyncService;

    @Operation(summary = "법정동 코드 파일 upsert 동기화")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ApiResponse<Map<String, Object>> sync() {
        LegalRegionSyncService.SyncResult result = legalRegionSyncService.syncFromClasspath();
        return ApiResponse.onSuccess(
                RegionSuccessCode.REGION_SYNC_200,
                Map.of(
                        "processed", result.processed(),
                        "totalAfter", result.totalAfter()
                )
        );
    }
}

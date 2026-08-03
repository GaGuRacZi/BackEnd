package com.gaguraczi.paw.domain.region.dto.res;

import com.gaguraczi.paw.domain.region.service.LegalRegionSyncService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "법정동 동기화 결과")
public record RegionSyncRes(
        int processed,
        long totalAfter
) {
    public static RegionSyncRes from(LegalRegionSyncService.SyncResult result) {
        return new RegionSyncRes(result.processed(), result.totalAfter());
    }
}

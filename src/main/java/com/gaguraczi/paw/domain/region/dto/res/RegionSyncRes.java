package com.gaguraczi.paw.domain.region.dto.res;

import com.gaguraczi.paw.domain.region.service.LegalRegionSyncService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "법정동 동기화 결과")
public record RegionSyncRes(
        @Schema(description = "처리(upsert) 건수", example = "20500")
        int processed,
        @Schema(description = "동기화 후 전체 건수", example = "20500")
        long totalAfter
) {
    public static RegionSyncRes from(LegalRegionSyncService.SyncResult result) {
        return new RegionSyncRes(result.processed(), result.totalAfter());
    }
}

package com.gaguraczi.paw.domain.breed.dto.res;

import com.gaguraczi.paw.domain.breed.service.BreedSyncService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "품종 동기화 결과")
public record BreedSyncRes(
        int dogProcessed,
        int catProcessed,
        long totalAfter
) {
    public static BreedSyncRes from(BreedSyncService.SyncResult result) {
        return new BreedSyncRes(result.dogProcessed(), result.catProcessed(), result.totalAfter());
    }
}

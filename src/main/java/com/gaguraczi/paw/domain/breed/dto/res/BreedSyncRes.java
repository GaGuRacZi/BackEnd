package com.gaguraczi.paw.domain.breed.dto.res;

import com.gaguraczi.paw.domain.breed.service.BreedSyncService;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "품종 동기화 결과")
public record BreedSyncRes(
        @Schema(description = "강아지 품종 처리 건수", example = "120")
        int dogProcessed,
        @Schema(description = "고양이 품종 처리 건수", example = "80")
        int catProcessed,
        @Schema(description = "동기화 후 전체 품종 수", example = "200")
        long totalAfter
) {
    public static BreedSyncRes from(BreedSyncService.SyncResult result) {
        return new BreedSyncRes(result.dogProcessed(), result.catProcessed(), result.totalAfter());
    }
}

package com.gaguraczi.paw.domain.breed.controller;

import com.gaguraczi.paw.domain.breed.exception.code.BreedSuccessCode;
import com.gaguraczi.paw.domain.breed.service.BreedSyncService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "admin-breeds", description = "품종 관리 API")
@RestController
@RequestMapping("/admin/breeds")
@RequiredArgsConstructor
public class AdminBreedController {

    private final BreedSyncService breedSyncService;

    @Operation(summary = "품종 파일 upsert 동기화 (breed-dog.txt / breed-cat.txt)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ApiResponse<Map<String, Object>> sync() {
        BreedSyncService.SyncResult result = breedSyncService.syncFromClasspath();
        return ApiResponse.onSuccess(
                BreedSuccessCode.BREED_SYNC_200,
                Map.of(
                        "dogProcessed", result.dogProcessed(),
                        "catProcessed", result.catProcessed(),
                        "totalAfter", result.totalAfter()
                )
        );
    }
}

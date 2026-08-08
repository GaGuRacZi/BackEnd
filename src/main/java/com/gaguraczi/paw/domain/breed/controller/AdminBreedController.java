package com.gaguraczi.paw.domain.breed.controller;

import com.gaguraczi.paw.domain.breed.dto.res.BreedSyncRes;
import com.gaguraczi.paw.domain.breed.exception.code.BreedSuccessCode;
import com.gaguraczi.paw.domain.breed.service.BreedSyncService;
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

@Tag(name = "admin-breeds", description = "품종 관리 API")
@RestController
@RequestMapping("/admin/breeds")
@RequiredArgsConstructor
public class AdminBreedController {

    private final BreedSyncService breedSyncService;

    @Operation(
            summary = "품종 파일 upsert 동기화",
            description = "ADMIN 역할 필요. classpath의 breed-dog.txt / breed-cat.txt를 upsert합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "BREED_SYNC_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "BREED_SYNC_200",
                                              "message": "품종 동기화에 성공했습니다.",
                                              "result": {
                                                "dogProcessed": 120,
                                                "catProcessed": 80,
                                                "totalAfter": 200
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
                            examples = @ExampleObject(
                                    name = "FORBIDDEN",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "JWT_403_2",
                                              "message": "유효하지 않은 token입니다.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/sync")
    public ApiResponse<BreedSyncRes> sync() {
        BreedSyncService.SyncResult result = breedSyncService.syncFromClasspath();
        return ApiResponse.onSuccess(
                BreedSuccessCode.BREED_SYNC_200,
                BreedSyncRes.from(result)
        );
    }
}

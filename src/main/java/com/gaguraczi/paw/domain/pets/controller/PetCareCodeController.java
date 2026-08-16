package com.gaguraczi.paw.domain.pets.controller;

import com.gaguraczi.paw.domain.pets.dto.req.PetCareSelectionReq;
import com.gaguraczi.paw.domain.pets.dto.res.PetCareCodeRes;
import com.gaguraczi.paw.domain.pets.enums.PetCareCategory;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.pets.exception.code.PetSuccessCode;
import com.gaguraczi.paw.domain.pets.service.PetCareCodeService;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.exception.GeneralException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 피해야 할 원료(ingredients) / 수술 이력(surgeries) / 관리 부위(care-areas) 공통 API.
 * 화면 구조가 동일하여 {domain} path segment로 카테고리를 분기하는 단일 컨트롤러로 구현한다.
 */
@Tag(name = "pets", description = "반려동물 API")
@RestController
@RequiredArgsConstructor
public class PetCareCodeController {

    private final PetCareCodeService petCareCodeService;

    @Operation(
            summary = "원료/수술이력/관리부위 마스터 코드 검색",
            description = "domain: ingredients(원료) | surgeries(수술이력) | care-areas(관리부위). keyword/petType은 선택."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "지원하지 않는 domain",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_400_4",
                                    value = "{\"isSuccess\":false,\"code\":\"PET_400_4\",\"message\":\"지원하지 않는 카테고리입니다.\",\"result\":null}"
                            )
                    )
            )
    })
    @GetMapping("/pets/{domain}/codes")
    public ApiResponse<List<PetCareCodeRes>> searchCodes(
            @Parameter(description = "ingredients | surgeries | care-areas", example = "ingredients")
            @PathVariable String domain,
            @Parameter(description = "검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "반려동물 종류") @RequestParam(required = false) PetType petType
    ) {
        PetCareCategory category = resolveCategory(domain);
        return ApiResponse.onSuccess(
                PetSuccessCode.PET_CODE_LIST_200,
                petCareCodeService.searchCodes(category, petType, keyword)
        );
    }

    @Operation(
            summary = "반려동물이 선택한 원료/수술이력/관리부위 조회",
            description = "Access Token(JWT) 필수. 본인 소유 펫만 조회 가능."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "펫 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_404",
                                    value = "{\"isSuccess\":false,\"code\":\"PET_404\",\"message\":\"펫을 찾을 수 없습니다.\",\"result\":null}"
                            )
                    )
            )
    })
    @GetMapping("/pets/{petId}/{domain}")
    public ApiResponse<List<PetCareCodeRes>> getSelections(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "ingredients | surgeries | care-areas", example = "ingredients")
            @PathVariable String domain
    ) {
        PetCareCategory category = resolveCategory(domain);
        return ApiResponse.onSuccess(
                PetSuccessCode.PET_CODE_GET_200,
                petCareCodeService.getSelections(petId, category)
        );
    }

    @Operation(
            summary = "반려동물 원료/수술이력/관리부위 선택 저장 (전체 갈아끼우기)",
            description = "Access Token(JWT) 필수. 요청에 담긴 codeIds로 기존 선택을 전부 대체합니다. 없는 코드가 포함되면 PET_404_2."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "저장 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "펫 없음 / 코드 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "PET_404",
                                            value = "{\"isSuccess\":false,\"code\":\"PET_404\",\"message\":\"펫을 찾을 수 없습니다.\",\"result\":null}"
                                    ),
                                    @ExampleObject(
                                            name = "PET_404_2",
                                            value = "{\"isSuccess\":false,\"code\":\"PET_404_2\",\"message\":\"존재하지 않는 코드가 포함되어 있습니다.\",\"result\":null}"
                                    )
                            }
                    )
            )
    })
    @PutMapping("/pets/{petId}/{domain}")
    public ApiResponse<List<PetCareCodeRes>> replaceSelections(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "ingredients | surgeries | care-areas", example = "ingredients")
            @PathVariable String domain,
            @Valid @RequestBody PetCareSelectionReq req
    ) {
        PetCareCategory category = resolveCategory(domain);
        return ApiResponse.onSuccess(
                PetSuccessCode.PET_CODE_UPDATE_200,
                petCareCodeService.replaceSelections(petId, category, req.codeIds())
        );
    }

    private static PetCareCategory resolveCategory(String domain) {
        return switch (domain) {
            case "ingredients" -> PetCareCategory.INGREDIENT;
            case "surgeries" -> PetCareCategory.SURGERY;
            case "care-areas" -> PetCareCategory.CARE_AREA;
            default -> throw GeneralException.of(PetErrorCode.PET_CATEGORY_INVALID);
        };
    }
}

package com.gaguraczi.paw.domain.breed.controller;

import com.gaguraczi.paw.domain.breed.dto.res.BreedRes;
import com.gaguraczi.paw.domain.breed.exception.code.BreedSuccessCode;
import com.gaguraczi.paw.domain.breed.service.BreedService;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "breeds", description = "반려동물 품종 API")
@RestController
@RequestMapping("/breeds")
@RequiredArgsConstructor
public class BreedController {

    private final BreedService breedService;

    @Operation(summary = "품종 검색/인기 조회")
    @GetMapping
    public ApiResponse<List<BreedRes>> search(
            @RequestParam PetType petType,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "false") boolean popularOnly
    ) {
        return ApiResponse.onSuccess(
                BreedSuccessCode.BREED_SEARCH_200,
                breedService.search(petType, q, popularOnly)
        );
    }
}

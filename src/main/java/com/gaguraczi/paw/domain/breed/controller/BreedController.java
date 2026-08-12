package com.gaguraczi.paw.domain.breed.controller;

import com.gaguraczi.paw.domain.breed.dto.res.BreedRes;
import com.gaguraczi.paw.domain.breed.exception.code.BreedSuccessCode;
import com.gaguraczi.paw.domain.breed.service.BreedService;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "품종 검색/인기 조회",
            description = """
                    JWT 필수.
                    - petType: DOG | CAT (필수)
                    - q: 품종명 부분 검색 (선택). 없으면 전체(또는 인기) 목록
                    - popularOnly=true: 인기 품종만
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "BREED_SEARCH_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "BREED_SEARCH_200",
                                              "message": "품종 조회에 성공했습니다.",
                                              "result": [
                                                {
                                                  "breedId": 1,
                                                  "petType": "DOG",
                                                  "name": "말티즈",
                                                  "popular": true
                                                },
                                                {
                                                  "breedId": 2,
                                                  "petType": "DOG",
                                                  "name": "푸들",
                                                  "popular": true
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "COMMON_400 / BREED_400",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "petType 누락",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON_400",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "지원하지 않는 petType",
                                            value = """
                                                    {
                                                      "isSuccess": false,
                                                      "code": "COMMON_400",
                                                      "message": "잘못된 요청입니다.",
                                                      "result": null
                                                    }
                                                    """
                                    )
                            }
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
            )
    })
    @GetMapping
    public ApiResponse<List<BreedRes>> search(
            @Parameter(description = "반려동물 종류", example = "DOG", required = true)
            @RequestParam PetType petType,
            @Parameter(description = "품종명 검색어 (부분 일치)", example = "말티")
            @RequestParam(required = false) String q,
            @Parameter(description = "인기 품종만 조회", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean popularOnly
    ) {
        return ApiResponse.onSuccess(
                BreedSuccessCode.BREED_SEARCH_200,
                breedService.search(petType, q, popularOnly)
        );
    }
}

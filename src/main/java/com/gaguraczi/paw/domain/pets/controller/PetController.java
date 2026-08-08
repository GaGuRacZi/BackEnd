package com.gaguraczi.paw.domain.pets.controller;

import com.gaguraczi.paw.domain.pets.dto.req.PetCreateReq;
import com.gaguraczi.paw.domain.pets.dto.req.PetUpdateReq;
import com.gaguraczi.paw.domain.pets.dto.res.PetRes;
import com.gaguraczi.paw.domain.pets.exception.code.PetSuccessCode;
import com.gaguraczi.paw.domain.pets.service.PetService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "pets", description = "반려동물 API")
@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @Operation(
            summary = "펫 등록 (프로필 이미지 1장)",
            description = """
                    Access Token(JWT) 필수. multipart/form-data: data(JSON, 필수) + image(파일, 선택, 1장).
                    breedId 또는 breed(품종명) 중 하나는 필수(PET_400_1).
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PetCreateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = @ExampleObject(
                                    name = "data JSON 예시",
                                    value = """
                                            {
                                              "petType": "DOG",
                                              "breedId": 1,
                                              "breed": "말티즈",
                                              "petName": "초코",
                                              "birth": "2022-01-15",
                                              "petWeight": 3.50,
                                              "gender": "MALE",
                                              "neutering": true
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_CREATE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PET_CREATE_200",
                                              "message": "펫이 등록되었습니다.",
                                              "result": {
                                                "petId": 1,
                                                "petType": "DOG",
                                                "breedId": 1,
                                                "breedName": "말티즈",
                                                "petName": "초코",
                                                "birth": "2022-01-15",
                                                "petWeight": 3.50,
                                                "gender": "MALE",
                                                "neutering": true,
                                                "main": true,
                                                "profileUrl": "https://cdn.example.com/pets/1.jpg"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성/품종/이미지 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "COMMON_400",
                                            value = """
                                                    {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PET_400_1",
                                            value = """
                                                    {"isSuccess":false,"code":"PET_400_1","message":"품종 ID 또는 품종명 중 하나는 필수입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PET_400_2",
                                            value = """
                                                    {"isSuccess":false,"code":"PET_400_2","message":"비어 있는 이미지 파일은 업로드할 수 없습니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PET_400",
                                            value = """
                                                    {"isSuccess":false,"code":"PET_400","message":"펫 요청 처리에 실패했습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "유효하지 않은 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetRes> create(
            @RequestPart("data") @Valid PetCreateReq data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(PetSuccessCode.PET_CREATE_200, petService.create(data, image));
    }

    @Operation(
            summary = "펫 수정 (프로필 이미지 1장)",
            description = """
                    Access Token(JWT) 필수. multipart/form-data: data(JSON, 선택) + image(파일, 선택, 1장).
                    본인 소유 펫만 수정 가능. 없으면 PET_404.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PetUpdateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = @ExampleObject(
                                    name = "data JSON 예시",
                                    value = """
                                            {
                                              "petName": "초코",
                                              "petWeight": 4.00,
                                              "neutering": true
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_UPDATE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PET_UPDATE_200",
                                              "message": "펫 정보가 수정되었습니다.",
                                              "result": {
                                                "petId": 1,
                                                "petType": "DOG",
                                                "breedId": 1,
                                                "breedName": "말티즈",
                                                "petName": "초코",
                                                "birth": "2022-01-15",
                                                "petWeight": 4.00,
                                                "gender": "MALE",
                                                "neutering": true,
                                                "main": true,
                                                "profileUrl": "https://cdn.example.com/pets/1.jpg"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성/이미지 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "COMMON_400",
                                            value = """
                                                    {"isSuccess":false,"code":"COMMON_400","message":"잘못된 요청입니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PET_400",
                                            value = """
                                                    {"isSuccess":false,"code":"PET_400","message":"펫 요청 처리에 실패했습니다.","result":null}
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "PET_400_2",
                                            value = """
                                                    {"isSuccess":false,"code":"PET_400_2","message":"비어 있는 이미지 파일은 업로드할 수 없습니다.","result":null}
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT 만료/미인증",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "유효하지 않은 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_403_2","message":"유효하지 않은 token입니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "펫 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_404",
                                    value = """
                                            {"isSuccess":false,"code":"PET_404","message":"펫을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PutMapping(value = "/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetRes> update(
            @Parameter(description = "수정할 펫 ID", example = "1") @PathVariable Long petId,
            @RequestPart(value = "data", required = false) @Valid PetUpdateReq data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(PetSuccessCode.PET_UPDATE_200, petService.update(petId, data, image));
    }

    @Schema(name = "PetCreateMultipart", description = "펫 등록 multipart")
    public static class PetCreateMultipart {
        @Schema(description = "펫 정보 JSON", implementation = PetCreateReq.class)
        public PetCreateReq data;

        @Schema(description = "펫 프로필 이미지 1장", type = "string", format = "binary")
        public MultipartFile image;
    }

    @Schema(name = "PetUpdateMultipart", description = "펫 수정 multipart")
    public static class PetUpdateMultipart {
        @Schema(description = "펫 수정 정보 JSON", implementation = PetUpdateReq.class)
        public PetUpdateReq data;

        @Schema(description = "펫 프로필 이미지 1장", type = "string", format = "binary")
        public MultipartFile image;
    }
}

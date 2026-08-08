package com.gaguraczi.paw.domain.pets.controller;

import com.gaguraczi.paw.domain.pets.dto.req.PetCreateReq;
import com.gaguraczi.paw.domain.pets.dto.req.PetUpdateReq;
import com.gaguraczi.paw.domain.pets.dto.res.PetRes;
import com.gaguraczi.paw.domain.pets.exception.code.PetSuccessCode;
import com.gaguraczi.paw.domain.pets.service.PetService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
            description = "multipart/form-data: data(JSON) + image(파일, 선택, 1장)",
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetRes> create(
            @RequestPart("data") @Valid PetCreateReq data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(PetSuccessCode.PET_CREATE_200, petService.create(data, image));
    }

    @Operation(
            summary = "펫 수정 (프로필 이미지 1장)",
            description = "multipart/form-data: data(JSON, 선택) + image(파일, 선택, 1장)",
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
    @PutMapping(value = "/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetRes> update(
            @PathVariable Long petId,
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

package com.gaguraczi.paw.domain.pets.controller;

import com.gaguraczi.paw.domain.pets.dto.req.PetRegistrationReq;
import com.gaguraczi.paw.domain.pets.dto.res.PetRegistrationRes;
import com.gaguraczi.paw.domain.pets.exception.code.PetSuccessCode;
import com.gaguraczi.paw.domain.pets.service.PetRegistrationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "pets", description = "반려동물 API")
@RestController
@RequestMapping("/pets/{petId}/registration")
@RequiredArgsConstructor
public class PetRegistrationController {

    private final PetRegistrationService petRegistrationService;

    @Operation(
            summary = "동물등록증 조회",
            description = "Access Token(JWT) 필수. 본인 소유 펫만 조회 가능. 등록된 동물등록증이 없으면 PET_404_1."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "펫 없음 / 동물등록증 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "PET_404",
                                            value = "{\"isSuccess\":false,\"code\":\"PET_404\",\"message\":\"펫을 찾을 수 없습니다.\",\"result\":null}"
                                    ),
                                    @ExampleObject(
                                            name = "PET_404_1",
                                            value = "{\"isSuccess\":false,\"code\":\"PET_404_1\",\"message\":\"등록된 동물등록증 정보가 없습니다.\",\"result\":null}"
                                    )
                            }
                    )
            )
    })
    @GetMapping
    public ApiResponse<PetRegistrationRes> get(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId
    ) {
        return ApiResponse.onSuccess(PetSuccessCode.PET_REGISTRATION_GET_200, petRegistrationService.get(petId));
    }

    @Operation(
            summary = "동물등록증 등록/수정 (upsert)",
            description = """
                    Access Token(JWT) 필수. multipart/form-data: data(JSON, 필수) + photo(파일, 선택).
                    본인 소유 펫만 가능. 등록증이 없으면 새로 생성, 있으면 수정합니다.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PetRegistrationMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "photo", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "저장 성공",
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
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetRegistrationRes> upsert(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @RequestPart("data") @Valid PetRegistrationReq data,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        return ApiResponse.onSuccess(
                PetSuccessCode.PET_REGISTRATION_UPDATE_200,
                petRegistrationService.upsert(petId, data, photo)
        );
    }

    @Schema(name = "PetRegistrationMultipart", description = "동물등록증 등록/수정 multipart")
    public static class PetRegistrationMultipart {
        @Schema(description = "동물등록증 정보 JSON", implementation = PetRegistrationReq.class)
        public PetRegistrationReq data;

        @Schema(description = "동물등록증 사진", type = "string", format = "binary")
        public MultipartFile photo;
    }
}

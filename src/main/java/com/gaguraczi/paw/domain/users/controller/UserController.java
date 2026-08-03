package com.gaguraczi.paw.domain.users.controller;

import com.gaguraczi.paw.domain.users.dto.req.UserProfileUpdateReq;
import com.gaguraczi.paw.domain.users.dto.res.UserProfileRes;
import com.gaguraczi.paw.domain.users.exception.code.UserSuccessCode;
import com.gaguraczi.paw.domain.users.service.UserService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "users", description = "유저 프로필 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ApiResponse<UserProfileRes> getMyProfile() {
        return ApiResponse.onSuccess(UserSuccessCode.USER_PROFILE_200, userService.getMyProfile());
    }

    @Operation(
            summary = "내 프로필 수정 (이미지 포함 가능)",
            description = "multipart/form-data: data(JSON, 선택) + image(파일, 선택)",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UserProfileMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "image", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = @ExampleObject(
                                    name = "data JSON 예시",
                                    value = """
                                            {
                                              "name": "홍길동",
                                              "nickname": "길동이",
                                              "intro": "강아지와 산책하는 걸 좋아해요"
                                            }
                                            """
                            )
                    )
            )
    )
    @PutMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileRes> updateMyProfile(
            @RequestPart(value = "data", required = false) @Valid UserProfileUpdateReq data,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                UserSuccessCode.USER_PROFILE_UPDATE_200,
                userService.updateMyProfile(data, image)
        );
    }

    @Schema(name = "UserProfileMultipart", description = "유저 프로필 수정 multipart")
    public static class UserProfileMultipart {
        @Schema(description = "프로필 정보 JSON", implementation = UserProfileUpdateReq.class)
        public UserProfileUpdateReq data;

        @Schema(description = "프로필 이미지", type = "string", format = "binary")
        public MultipartFile image;
    }
}

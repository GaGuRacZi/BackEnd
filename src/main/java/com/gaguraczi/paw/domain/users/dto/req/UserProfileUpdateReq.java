package com.gaguraczi.paw.domain.users.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "유저 프로필 수정 요청 (multipart data part JSON, 모든 필드 선택)")
public class UserProfileUpdateReq {

    @Size(max = 10, message = "이름은 10자 이내이어야 합니다.")
    @Schema(description = "보호자 이름 (최대 10자)", example = "홍길동")
    private String name;

    @Size(max = 15, message = "닉네임은 15자 이내이어야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 15자 이내의 영문, 숫자, 한글만 사용 가능합니다.")
    @Schema(description = "닉네임 (15자 이내 영문/숫자/한글)", example = "길동이")
    private String nickname;

    @Size(max = 30, message = "한줄소개는 30자 이내이어야 합니다.")
    @Schema(description = "한줄소개 (최대 30자)", example = "강아지와 산책하는 걸 좋아해요")
    private String intro;
}

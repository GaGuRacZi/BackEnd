package com.gaguraczi.paw.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OnboardingProfileReq {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 10, message = "이름은 10자 이내이어야 합니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 15, message = "닉네임은 15자 이내이어야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 15자 이내의 영문, 숫자, 한글만 사용 가능합니다.")
    private String nickname;

    @NotBlank(message = "한줄소개는 필수입니다.")
    @Size(max = 200, message = "한줄소개는 200자 이내이어야 합니다.")
    private String intro;
}

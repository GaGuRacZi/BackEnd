package com.gaguraczi.paw.domain.todo.dto.request;


import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TagCreateRequest(

            @NotBlank(message = "태그 이름은 필수입니다.")
            @Size(max = 36, message = "태그 이름은 36자 이하여야 합니다.")
            String tagName,

            @NotNull(message = "태그 색상은 필수입니다.")
            TagColorEnum tagColorEnum
    ) {
    }


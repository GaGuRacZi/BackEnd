package com.gaguraczi.paw.domain.todo.dto.request;

import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import jakarta.validation.constraints.Size;

    /**
     * 태그 수정 요청 DTO
     * PATCH 방식이므로 두 필드 모두 null 허용(전달된 필드만 수정).
     * 단, 둘 다 null이면 Service에서 400을 반환하도록 처리한다.
     */
    public record TagUpdateRequest(

            @Size(max = 36, message = "태그 이름은 36자 이하여야 합니다.")
            String tagName,

            TagColorEnum.TagColor tagColor
    ) {
        public boolean isEmpty() {
            return tagName == null && tagColor == null;
        }
    }


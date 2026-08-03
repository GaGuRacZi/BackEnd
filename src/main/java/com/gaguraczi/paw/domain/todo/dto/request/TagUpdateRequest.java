package com.gaguraczi.paw.domain.todo.dto.request;

import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import jakarta.validation.constraints.Size;


    public record TagUpdateRequest(

            @Size(max = 36, message = "태그 이름은 36자 이하여야 합니다.")
            String tagName,

            TagColorEnum.TagColor tagColor
    ) {
        public boolean isEmpty() {
            return tagName == null && tagColor == null;
        }
    }


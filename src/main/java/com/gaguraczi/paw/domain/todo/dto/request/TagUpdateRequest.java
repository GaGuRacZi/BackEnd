package com.gaguraczi.paw.domain.todo.dto.request;

import com.google.firebase.remoteconfig.TagColor;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record TagUpdateRequest(

            @Size(max = 36, message = "태그 이름은 36자 이하여야 합니다.")
            @Pattern(regexp = "\\S(.*\\S)?", message = "태그 이름은 공백일 수 없습니다.")
            String tagName,

            TagColor tagColor
    ) {
    public TagUpdateRequest {
        tagName = (tagName == null) ? null : tagName.trim();
    }

    public boolean isEmpty() {
        return tagName == null && tagColor == null;
    }
}




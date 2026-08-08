package com.gaguraczi.paw.domain.todo.dto.response;

import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColor;

public record TagResponse(
        Long tagId,
        String tagName,
        TagColor tagColor
) {
    public static TagResponse from(TagEntity tag) {
        return new TagResponse(
                tag.getTagId(),
                tag.getTagName(),
                tag.getTagColor()
        );
    }
}
package com.gaguraczi.paw.domain.todo.dto.response;

import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;

public record TagResponse(
        Long tagId,
        String tagName,
        TagColorEnum.TagColor tagColor
) {
    public static TagResponse from(TagEntity.Tag tag) {
        return new TagResponse(
                tag.getTagId(),
                tag.getTagName(),
                tag.getTagColor()
        );
    }
}
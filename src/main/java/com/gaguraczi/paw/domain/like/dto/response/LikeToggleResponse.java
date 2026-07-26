package com.gaguraczi.paw.domain.like.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeToggleResponse {

    private final boolean liked;
    private final Long likeCount;
}

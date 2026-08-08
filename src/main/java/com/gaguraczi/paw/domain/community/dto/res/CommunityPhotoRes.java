package com.gaguraczi.paw.domain.community.dto.res;

import com.gaguraczi.paw.domain.community.entity.CommunityPhoto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "커뮤니티 게시글 사진")
public class CommunityPhotoRes {

    @Schema(example = "1")
    private final Long photoId;
    @Schema(example = "https://cdn.example.com/community/10/a.jpg")
    private final String url;
    @Schema(example = "true")
    private final boolean isThumbnail;
    @Schema(example = "0")
    private final int sortOrder;

    public static CommunityPhotoRes from(CommunityPhoto photo) {
        return CommunityPhotoRes.builder()
                .photoId(photo.getPhotoId())
                .url(photo.getPhotoS3Url())
                .isThumbnail(Boolean.TRUE.equals(photo.getIsThumbnail()))
                .sortOrder(photo.getSortOrder() == null ? 0 : photo.getSortOrder())
                .build();
    }
}

package com.gaguraczi.paw.domain.weights.dto.res;

import com.gaguraczi.paw.domain.weights.entity.PetWeightPhoto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "체중 기록 메모 사진")
public class PetWeightPhotoRes {

    @Schema(example = "1")
    private final Long photoId;
    @Schema(example = "https://cdn.example.com/pet-weight/1/a.jpg")
    private final String url;
    @Schema(example = "0")
    private final int sortOrder;

    public static PetWeightPhotoRes from(PetWeightPhoto photo) {
        return PetWeightPhotoRes.builder()
                .photoId(photo.getPhotoId())
                .url(photo.getPhotoS3Url())
                .sortOrder(photo.getSortOrder() == null ? 0 : photo.getSortOrder())
                .build();
    }
}

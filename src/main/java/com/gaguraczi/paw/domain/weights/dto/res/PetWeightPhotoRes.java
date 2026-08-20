package com.gaguraczi.paw.domain.weights.dto.res;

import com.gaguraczi.paw.domain.weights.entity.PetWeightPhoto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체중 기록 메모 사진")
public record PetWeightPhotoRes(

        @Schema(example = "1")
        Long photoId,
        @Schema(example = "https://cdn.example.com/pet-weight/1/a.jpg")
        String url,
        @Schema(example = "0")
        int sortOrder
) {

    public static PetWeightPhotoRes from(PetWeightPhoto photo) {
        return new PetWeightPhotoRes(
                photo.getPhotoId(),
                photo.getPhotoS3Url(),
                photo.getSortOrder() == null ? 0 : photo.getSortOrder()
        );
    }
}

package com.gaguraczi.paw.domain.breed.dto.res;

import com.gaguraczi.paw.domain.breed.entity.Breed;
import com.gaguraczi.paw.domain.users.enums.PetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "품종 검색 응답")
public class BreedRes {

    @Schema(description = "품종 ID", example = "1")
    private final Long breedId;

    @Schema(description = "반려동물 종류", example = "DOG")
    private final PetType petType;

    @Schema(description = "품종명", example = "말티즈")
    private final String name;

    @Schema(description = "인기 품종 여부", example = "true")
    private final boolean popular;

    public static BreedRes from(Breed breed) {
        return BreedRes.builder()
                .breedId(breed.getBreedId())
                .petType(breed.getPetType())
                .name(breed.getName())
                .popular(breed.isPopular())
                .build();
    }
}
